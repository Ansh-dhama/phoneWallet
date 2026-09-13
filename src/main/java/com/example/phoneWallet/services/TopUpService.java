package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.InvalidTopUpException;
import com.example.phoneWallet.Exceptions.LedgerValidationException;
import com.example.phoneWallet.Repository.TopUpIntentRepository;
import com.example.phoneWallet.Repository.TransactionRepository;
import com.example.phoneWallet.Util.CurrencyUtil;
import com.example.phoneWallet.Util.IdempotencyKeyUtil;
import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.*;
import com.example.phoneWallet.enums.*;
import com.example.phoneWallet.kafka.KafkaTopics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TopUpService {
    private final TopUpIntentRepository topUpRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final AccessControlService accessControlService;
    private final LedgerService ledgerService;
    private final AuditLedgerService auditLedgerService;
    private final OutboxEventService outboxEventService;
    private final AuditLogService auditLogService;
    private final RazorpayPaymentService razorpayPaymentService;
    private final BigDecimal maxAmount;

    public TopUpService(TopUpIntentRepository topUpRepository,
                        TransactionRepository transactionRepository,
                        WalletService walletService,
                        AccessControlService accessControlService,
                        LedgerService ledgerService,
                        AuditLedgerService auditLedgerService,
                        OutboxEventService outboxEventService,
                        AuditLogService auditLogService,
                        RazorpayPaymentService razorpayPaymentService,
                        @Value("${wallet.topup.max-amount:50000}") BigDecimal maxAmount) {
        this.topUpRepository = topUpRepository;
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.accessControlService = accessControlService;
        this.ledgerService = ledgerService;
        this.auditLedgerService = auditLedgerService;
        this.outboxEventService = outboxEventService;
        this.auditLogService = auditLogService;
        this.razorpayPaymentService = razorpayPaymentService;
        this.maxAmount = maxAmount;
    }

    /**
     * Creates a local PENDING top-up intent backed by a real Razorpay Order.
     * Wallet balance is never changed here.
     */
    @Transactional
    public TopUpIntentResponse initiate(Long walletId, TopUpIntentRequest request) {
        User user = accessControlService.currentUser();
        Wallet wallet = accessControlService.requireOwnedWallet(walletId);
        String currency = CurrencyUtil.normalize(request.currency());

        if (!wallet.getCurrency().equals(currency)) {
            throw new InvalidTopUpException(
                    "Top-up currency must match wallet currency " + wallet.getCurrency());
        }
        if (request.amount().compareTo(maxAmount) > 0) {
            throw new InvalidTopUpException("Top-up amount exceeds configured limit " + maxAmount);
        }
        if (!"INR".equals(currency)) {
            throw new InvalidTopUpException("Razorpay top-up is currently enabled only for INR wallets");
        }

        String scopedKey = IdempotencyKeyUtil.scoped(user.getId(), request.idempotencyKey());
        Optional<TopUpIntent> existing = topUpRepository.findByIdempotencyKey(scopedKey);
        if (existing.isPresent()) {
            TopUpIntent previous = existing.get();
            boolean sameRequest = previous.getWalletId().equals(walletId)
                    && previous.getAmount().compareTo(request.amount()) == 0
                    && previous.getCurrency().equals(currency);
            if (!sameRequest) {
                throw new InvalidTopUpException(
                        "Idempotency key was already used for a different top-up request");
            }
            return TopUpIntentResponse.from(previous, false);
        }

        String providerOrderId = razorpayPaymentService.createOrder(
                request.amount(), currency, walletId, user.getId());

        TopUpIntent intent = new TopUpIntent();
        intent.setWalletId(walletId);
        intent.setUserId(user.getId());
        intent.setAmount(request.amount());
        intent.setCurrency(currency);
        intent.setIdempotencyKey(scopedKey);
        intent.setProviderOrderId(providerOrderId);
        intent.setStatus(TopUpStatus.PENDING);

        TopUpIntent saved = topUpRepository.save(intent);
        auditLogService.logSuccess(
                AuditAction.TOPUP_INITIATED,
                user.getId(), user.getUsername(), user.getRole(),
                walletId, null,
                "Razorpay top-up order created; wallet balance unchanged until verified settlement");

        return TopUpIntentResponse.from(saved, false);
    }

    public RazorpayCheckoutConfigResponse checkoutConfig() {
        return new RazorpayCheckoutConfigResponse(razorpayPaymentService.publicKeyId());
    }

    public Page<TopUpIntentResponse> listMine(Long walletId, Pageable pageable) {
        accessControlService.requireWalletAccess(walletId);
        return topUpRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable)
                .map(i -> TopUpIntentResponse.from(i, false));
    }

    /**
     * Called only after Razorpay Checkout returns payment_id/order_id/signature.
     * The backend verifies the signature using the server-stored order id, fetches
     * the payment from Razorpay, checks order/amount/currency/captured status, and
     * only then credits the wallet.
     */
    @Transactional
    public TopUpIntentResponse verifyRazorpayPayment(
            Long intentId,
            RazorpayPaymentVerificationRequest request) {

        TopUpIntent intent = topUpRepository.findByIdForUpdate(intentId)
                .orElseThrow(() -> new InvalidTopUpException("Top-up intent not found"));

        accessControlService.requireOwnedWallet(intent.getWalletId());

        if (intent.getStatus() == TopUpStatus.COMPLETED) {
            if (request.razorpayPaymentId().equals(intent.getProviderPaymentId())) {
                return TopUpIntentResponse.from(intent, false);
            }
            throw new InvalidTopUpException("This top-up has already been completed");
        }
        if (intent.getStatus() != TopUpStatus.PENDING) {
            throw new InvalidTopUpException("Top-up intent is not payable");
        }

        String paymentId = razorpayPaymentService.verifyCapturedPayment(
                intent.getProviderOrderId(),
                intent.getAmount(),
                intent.getCurrency(),
                request);

        settleLocked(intent, paymentId);
        return TopUpIntentResponse.from(intent, false);
    }

    /**
     * Razorpay webhook fallback. This makes settlement resilient if the browser is
     * closed after payment but before the client verification request completes.
     */
    @Transactional
    public void handleRazorpayWebhook(String rawBody, String signature) {
        RazorpayPaymentService.RazorpayWebhookPayment event =
                razorpayPaymentService.verifyAndParseWebhook(rawBody, signature);

        if (event.orderId() == null || event.orderId().isBlank()) {
            return; // Ignore unrelated Razorpay events safely.
        }

        TopUpIntent intent = topUpRepository.findByProviderOrderIdForUpdate(event.orderId())
                .orElse(null);
        if (intent == null) {
            return; // Valid Razorpay event, but not one of this application's top-ups.
        }

        if (intent.getStatus() == TopUpStatus.COMPLETED) {
            return; // Webhooks can be duplicated or arrive out of order.
        }

        if ("payment.failed".equalsIgnoreCase(event.event())) {
            if (intent.getStatus() == TopUpStatus.PENDING) {
                intent.setStatus(TopUpStatus.FAILED);
                intent.setFailureReason(event.errorDescription() == null || event.errorDescription().isBlank()
                        ? "Razorpay reported payment failure"
                        : event.errorDescription());
                topUpRepository.save(intent);
            }
            return;
        }

        boolean capturedEvent = "payment.captured".equalsIgnoreCase(event.event())
                || "order.paid".equalsIgnoreCase(event.event());
        if (!capturedEvent || event.paymentId() == null || event.paymentId().isBlank()) {
            return;
        }

        validateWebhookPayment(intent, event);

        String paymentId = razorpayPaymentService.verifyCapturedPaymentById(
                intent.getProviderOrderId(),
                intent.getAmount(),
                intent.getCurrency(),
                event.paymentId());

        settleLocked(intent, paymentId);
    }

    private void validateWebhookPayment(
            TopUpIntent intent,
            RazorpayPaymentService.RazorpayWebhookPayment event) {

        if (event.amountSubunits() == null || event.currency() == null) {
            throw new InvalidTopUpException("Razorpay webhook is missing payment amount/currency");
        }
        long expectedAmount = razorpayPaymentService.toSubunits(intent.getAmount());
        if (event.amountSubunits() != expectedAmount) {
            throw new InvalidTopUpException("Razorpay webhook payment amount does not match top-up");
        }
        if (!intent.getCurrency().equalsIgnoreCase(event.currency())) {
            throw new InvalidTopUpException("Razorpay webhook currency does not match top-up");
        }
    }

    private void settleLocked(TopUpIntent intent, String providerPaymentId) {
        if (intent.getStatus() == TopUpStatus.COMPLETED) return;
        if (intent.getStatus() != TopUpStatus.PENDING) {
            throw new InvalidTopUpException("Top-up intent is not payable");
        }

        String txIdempotency = "topup-settlement:" + intent.getProviderOrderId();
        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(txIdempotency);
        if (existingTx.isPresent()) {
            intent.setStatus(TopUpStatus.COMPLETED);
            intent.setProviderPaymentId(providerPaymentId);
            intent.setTransactionReference(existingTx.get().getTransactionReference());
            intent.setCompletedAt(LocalDateTime.now());
            topUpRepository.save(intent);
            return;
        }

        Wallet wallet = walletService.credit(intent.getWalletId(), intent.getAmount());

        Transaction tx = new Transaction();
        tx.setTransactionReference("LOAD-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase());
        tx.setFromWalletId(null);
        tx.setToWalletId(intent.getWalletId());
        tx.setAmount(intent.getAmount());
        tx.setCurrency(intent.getCurrency());
        tx.setIdempotencyKey(txIdempotency);
        tx.setType(TransactionType.LOAD_MONEY);
        tx.setStatus(TransactionStatus.INITIATED);
        tx.setProviderReference(providerPaymentId);
        tx.setRefundedAmount(BigDecimal.ZERO);
        tx = transactionRepository.save(tx);

        ledgerService.recordExternalFundingDebit(tx.getTransactionReference(), intent.getAmount());
        ledgerService.recordEntry(
                tx.getTransactionReference(),
                wallet.getId(),
                EntryType.CREDIT,
                intent.getAmount(),
                wallet.getBalance());

        if (!auditLedgerService.verifyTransaction(tx.getTransactionReference())) {
            throw new LedgerValidationException("Top-up settlement ledger is not balanced");
        }

        tx.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTx = transactionRepository.save(tx);

        TransactionEvent event = new TransactionEvent();
        event.setTransactionReference(savedTx.getTransactionReference());
        event.setFromWalletId(null);
        event.setToWalletId(savedTx.getToWalletId());
        event.setAmount(savedTx.getAmount());
        event.setCurrency(savedTx.getCurrency());
        event.setType(savedTx.getType());
        event.setStatus(savedTx.getStatus());
        event.setCreatedAt(savedTx.getCreatedAt());
        event.setMessage("Verified Razorpay top-up completed successfully");
        outboxEventService.saveEvent("TRANSACTION_SUCCESS", KafkaTopics.TRANSACTION_SUCCESS, event);

        intent.setProviderPaymentId(providerPaymentId);
        intent.setTransactionReference(savedTx.getTransactionReference());
        intent.setStatus(TopUpStatus.COMPLETED);
        intent.setFailureReason(null);
        intent.setCompletedAt(LocalDateTime.now());
        topUpRepository.save(intent);

        auditLogService.logSuccess(
                AuditAction.TOPUP_COMPLETED,
                intent.getUserId(), null, Role.USER,
                intent.getWalletId(), savedTx.getTransactionReference(),
                "Verified Razorpay top-up settled successfully");
    }
}
