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
    private final TopUpSignatureService signatureService;
    private final boolean demoEnabled;
    private final BigDecimal maxAmount;

    public TopUpService(TopUpIntentRepository topUpRepository, TransactionRepository transactionRepository,
                        WalletService walletService, AccessControlService accessControlService, LedgerService ledgerService,
                        AuditLedgerService auditLedgerService, OutboxEventService outboxEventService, AuditLogService auditLogService,
                        TopUpSignatureService signatureService,
                        @Value("${wallet.topup.demo-enabled:false}") boolean demoEnabled,
                        @Value("${wallet.topup.max-amount:50000}") BigDecimal maxAmount) {
        this.topUpRepository = topUpRepository;
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.accessControlService = accessControlService;
        this.ledgerService = ledgerService;
        this.auditLedgerService = auditLedgerService;
        this.outboxEventService = outboxEventService;
        this.auditLogService = auditLogService;
        this.signatureService = signatureService;
        this.demoEnabled = demoEnabled;
        this.maxAmount = maxAmount;
    }

    @Transactional
    public TopUpIntentResponse initiate(Long walletId, TopUpIntentRequest request) {
        User user = accessControlService.currentUser();
        Wallet wallet = accessControlService.requireOwnedWallet(walletId);
        String currency = CurrencyUtil.normalize(request.currency());
        if (!wallet.getCurrency().equals(currency)) throw new InvalidTopUpException("Top-up currency must match wallet currency " + wallet.getCurrency());
        if (request.amount().compareTo(maxAmount) > 0) throw new InvalidTopUpException("Top-up amount exceeds configured limit " + maxAmount);

        String scopedKey = IdempotencyKeyUtil.scoped(user.getId(), request.idempotencyKey());
        Optional<TopUpIntent> existing = topUpRepository.findByIdempotencyKey(scopedKey);
        if (existing.isPresent()) {
            TopUpIntent previous = existing.get();
            boolean sameRequest = previous.getWalletId().equals(walletId)
                    && previous.getAmount().compareTo(request.amount()) == 0
                    && previous.getCurrency().equals(currency);
            if (!sameRequest) {
                throw new InvalidTopUpException("Idempotency key was already used for a different top-up request");
            }
            return TopUpIntentResponse.from(previous, demoEnabled);
        }

        TopUpIntent intent = new TopUpIntent();
        intent.setWalletId(walletId);
        intent.setUserId(user.getId());
        intent.setAmount(request.amount());
        intent.setCurrency(currency);
        intent.setIdempotencyKey(scopedKey);
        intent.setProviderOrderId("TOPUP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        intent.setStatus(TopUpStatus.PENDING);
        TopUpIntent saved = topUpRepository.save(intent);
        auditLogService.logSuccess(AuditAction.TOPUP_INITIATED, user.getId(), user.getUsername(), user.getRole(), walletId, null,
                "Top-up intent created; wallet balance is unchanged until provider settlement");
        return TopUpIntentResponse.from(saved, demoEnabled);
    }

    public Page<TopUpIntentResponse> listMine(Long walletId, Pageable pageable) {
        accessControlService.requireWalletAccess(walletId);
        return topUpRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable)
                .map(i -> TopUpIntentResponse.from(i, demoEnabled));
    }

    @Transactional
    public TopUpIntentResponse completeDemo(Long intentId) {
        if (!demoEnabled) throw new InvalidTopUpException("Demo payment completion is disabled");
        TopUpIntent intent = topUpRepository.findByIdForUpdate(intentId)
                .orElseThrow(() -> new InvalidTopUpException("Top-up intent not found"));
        accessControlService.requireOwnedWallet(intent.getWalletId());
        settleLocked(intent, "DEMO-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return TopUpIntentResponse.from(intent, true);
    }

    @Transactional
    public TopUpIntentResponse handleWebhook(TopUpWebhookRequest request, String signature) {
        signatureService.verify(request, signature);
        TopUpIntent intent = topUpRepository.findByProviderOrderIdForUpdate(request.providerOrderId())
                .orElseThrow(() -> new InvalidTopUpException("Unknown provider order id"));

        String currency = CurrencyUtil.normalize(request.currency());
        if (intent.getAmount().compareTo(request.amount()) != 0 || !intent.getCurrency().equals(currency)) {
            throw new InvalidTopUpException("Webhook amount/currency does not match the top-up intent");
        }
        if (intent.getStatus() == TopUpStatus.COMPLETED) return TopUpIntentResponse.from(intent, demoEnabled);
        if ("FAILED".equalsIgnoreCase(request.status())) {
            intent.setStatus(TopUpStatus.FAILED);
            intent.setFailureReason("Payment provider reported failure");
            topUpRepository.save(intent);
            return TopUpIntentResponse.from(intent, demoEnabled);
        }
        settleLocked(intent, request.providerPaymentId());
        return TopUpIntentResponse.from(intent, demoEnabled);
    }

    private void settleLocked(TopUpIntent intent, String providerPaymentId) {
        if (intent.getStatus() == TopUpStatus.COMPLETED) return;
        if (intent.getStatus() != TopUpStatus.PENDING) throw new InvalidTopUpException("Top-up intent is not payable");

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
        tx.setTransactionReference("LOAD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
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
        ledgerService.recordEntry(tx.getTransactionReference(), wallet.getId(), EntryType.CREDIT, intent.getAmount(), wallet.getBalance());
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
        event.setMessage("Verified top-up completed successfully");
        outboxEventService.saveEvent("TRANSACTION_SUCCESS", KafkaTopics.TRANSACTION_SUCCESS, event);

        intent.setProviderPaymentId(providerPaymentId);
        intent.setTransactionReference(savedTx.getTransactionReference());
        intent.setStatus(TopUpStatus.COMPLETED);
        intent.setFailureReason(null);
        intent.setCompletedAt(LocalDateTime.now());
        topUpRepository.save(intent);
        auditLogService.logSuccess(AuditAction.TOPUP_COMPLETED, intent.getUserId(), null, Role.USER, intent.getWalletId(),
                savedTx.getTransactionReference(), "Verified top-up settled successfully");
    }
}
