package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.*;
import com.example.phoneWallet.Repository.TransactionRepository;
import com.example.phoneWallet.Util.CurrencyUtil;
import com.example.phoneWallet.Util.IdempotencyKeyUtil;
import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.*;
import com.example.phoneWallet.kafka.KafkaTopics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final AuditLedgerService auditService;
    private final RiskService riskService;
    private final AuditLogService auditLogService;
    private final OutboxEventService outboxEventService;
    private final AccessControlService accessControlService;
    private final DistributedLockService distributedLockService;
    private final TransactionTemplate transactionTemplate;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletService walletService,
                              LedgerService ledgerService,
                              AuditLedgerService auditService,
                              RiskService riskService,
                              AuditLogService auditLogService,
                              OutboxEventService outboxEventService,
                              AccessControlService accessControlService,
                              DistributedLockService distributedLockService,
                              PlatformTransactionManager transactionManager) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.riskService = riskService;
        this.auditLogService = auditLogService;
        this.outboxEventService = outboxEventService;
        this.accessControlService = accessControlService;
        this.distributedLockService = distributedLockService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Transaction processTransaction(TransferRequest request) {
        validateTransferRequest(request);
        accessControlService.requireOwnedWallet(request.getFromWalletId());
        User actor = accessControlService.currentUser();
        String currency = CurrencyUtil.normalize(request.getCurrency());
        String scopedKey = IdempotencyKeyUtil.scoped(actor.getId(), request.getIdempotencyKey());
        return executeIdempotent(
                scopedKey,
                existing -> validateExisting(existing, TransactionType.TRANSFER, request.getFromWalletId(),
                        request.getToWalletId(), request.getAmount(), currency, null),
                () -> processTransferInternal(request, currency, scopedKey)
        );
    }

    private Transaction processTransferInternal(TransferRequest request, String currency, String scopedKey) {

        validateWalletCurrency(request.getFromWalletId(), currency);
        validateWalletCurrency(request.getToWalletId(), currency);
        riskService.validateRisk(request.getFromWalletId(), request.getAmount());

        Map<Long, Wallet> locked = walletService.lockWalletsInOrder(request.getFromWalletId(), request.getToWalletId());
        Wallet from = locked.get(request.getFromWalletId());
        Wallet to = locked.get(request.getToWalletId());

        Transaction tx = newTransaction("TXN", request.getFromWalletId(), request.getToWalletId(), request.getAmount(),
                currency, scopedKey, TransactionType.TRANSFER);
        tx = transactionRepository.save(tx);

        Wallet debited = walletService.debitLocked(from, request.getAmount());
        ledgerService.recordEntry(tx.getTransactionReference(), debited.getId(), EntryType.DEBIT, request.getAmount(), debited.getBalance());
        Wallet credited = walletService.creditLocked(to, request.getAmount());
        ledgerService.recordEntry(tx.getTransactionReference(), credited.getId(), EntryType.CREDIT, request.getAmount(), credited.getBalance());
        requireBalanced(tx.getTransactionReference(), "Transfer");

        tx.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(tx);
        writeSuccessOutbox(saved, "Transaction completed successfully", KafkaTopics.TRANSACTION_SUCCESS, "TRANSACTION_SUCCESS");
        auditSuccess(AuditAction.TRANSFER_SUCCESS, saved, "Wallet transfer completed successfully");
        return saved;
    }

    public Transaction payTransaction(PayRequest request) {
        validatePaymentRequest(request);
        accessControlService.requireOwnedWallet(request.getFromWalletId());
        accessControlService.requireMerchantWallet(request.getToWalletId());
        User actor = accessControlService.currentUser();
        String currency = CurrencyUtil.normalize(request.getCurrency());
        String scopedKey = IdempotencyKeyUtil.scoped(actor.getId(), request.getIdempotencyKey());
        return executeIdempotent(
                scopedKey,
                existing -> validateExisting(existing, TransactionType.MERCHANT_PAYMENT, request.getFromWalletId(),
                        request.getToWalletId(), request.getAmount(), currency, null),
                () -> processPaymentInternal(request, currency, scopedKey)
        );
    }

    private Transaction processPaymentInternal(PayRequest request, String currency, String scopedKey) {

        validateWalletCurrency(request.getFromWalletId(), currency);
        validateWalletCurrency(request.getToWalletId(), currency);
        riskService.validateRisk(request.getFromWalletId(), request.getAmount());

        Map<Long, Wallet> locked = walletService.lockWalletsInOrder(request.getFromWalletId(), request.getToWalletId());
        Wallet from = locked.get(request.getFromWalletId());
        Wallet to = locked.get(request.getToWalletId());

        Transaction tx = newTransaction("PAY", request.getFromWalletId(), request.getToWalletId(), request.getAmount(),
                currency, scopedKey, TransactionType.MERCHANT_PAYMENT);
        tx.setMerchantReference(request.getMerchantReference());
        tx = transactionRepository.save(tx);

        Wallet debited = walletService.debitLocked(from, request.getAmount());
        ledgerService.recordEntry(tx.getTransactionReference(), debited.getId(), EntryType.DEBIT, request.getAmount(), debited.getBalance());
        Wallet credited = walletService.creditLocked(to, request.getAmount());
        ledgerService.recordEntry(tx.getTransactionReference(), credited.getId(), EntryType.CREDIT, request.getAmount(), credited.getBalance());
        requireBalanced(tx.getTransactionReference(), "Merchant payment");

        tx.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(tx);
        writeSuccessOutbox(saved, "Merchant payment completed successfully", KafkaTopics.TRANSACTION_SUCCESS, "TRANSACTION_SUCCESS");
        auditSuccess(AuditAction.MERCHANT_PAYMENT_SUCCESS, saved, "Merchant payment completed successfully");
        return saved;
    }

    public Transaction refund(RefundRequest request) {
        if (request == null || request.getIdempotencyKey() == null) throw new IllegalArgumentException("Refund request is required");
        User actor = accessControlService.currentUser();
        String currency = CurrencyUtil.normalize(request.getCurrency());
        String scopedKey = IdempotencyKeyUtil.scoped(actor.getId(), request.getIdempotencyKey());
        return executeIdempotent(
                scopedKey,
                existing -> validateExisting(existing, TransactionType.REFUND, null, null, request.getRefundAmount(),
                        currency, request.getOriginalTransactionReference()),
                () -> refundInternal(request, currency, scopedKey)
        );
    }

    private Transaction refundInternal(RefundRequest request, String currency, String scopedKey) {

        Transaction original = transactionRepository.findByTransactionReferenceForUpdate(request.getOriginalTransactionReference())
                .orElseThrow(() -> new TransactionNotFoundException("Original transaction not found"));

        accessControlService.requireRefundAccess(original);
        validateRefund(request, original);
        if (!currency.equalsIgnoreCase(original.getCurrency())) {
            throw new InvalidTransactionException("Refund currency must match original transaction currency " + original.getCurrency());
        }

        riskService.validateRisk(original.getToWalletId(), request.getRefundAmount());
        Map<Long, Wallet> locked = walletService.lockWalletsInOrder(original.getToWalletId(), original.getFromWalletId());
        Wallet payee = locked.get(original.getToWalletId());
        Wallet payer = locked.get(original.getFromWalletId());

        Transaction refund = newTransaction("REFUND", original.getToWalletId(), original.getFromWalletId(), request.getRefundAmount(),
                original.getCurrency(), scopedKey, TransactionType.REFUND);
        refund.setRelatedTransactionReference(original.getTransactionReference());
        refund = transactionRepository.save(refund);

        Wallet debited = walletService.debitLocked(payee, request.getRefundAmount());
        ledgerService.recordEntry(refund.getTransactionReference(), debited.getId(), EntryType.REFUND_DEBIT, request.getRefundAmount(), debited.getBalance());
        Wallet credited = walletService.creditLocked(payer, request.getRefundAmount());
        ledgerService.recordEntry(refund.getTransactionReference(), credited.getId(), EntryType.REFUND_CREDIT, request.getRefundAmount(), credited.getBalance());
        requireBalanced(refund.getTransactionReference(), "Refund");

        BigDecimal alreadyRefunded = original.getRefundedAmount() == null ? BigDecimal.ZERO : original.getRefundedAmount();
        BigDecimal cumulative = alreadyRefunded.add(request.getRefundAmount());
        original.setRefundedAmount(cumulative);
        original.setStatus(cumulative.compareTo(original.getAmount()) == 0
                ? TransactionStatus.REFUNDED
                : TransactionStatus.PARTIALLY_REFUNDED);
        transactionRepository.save(original);

        refund.setStatus(TransactionStatus.SUCCESS);
        Transaction savedRefund = transactionRepository.save(refund);
        writeSuccessOutbox(savedRefund, "Refund completed successfully", KafkaTopics.REFUND_COMPLETED, "REFUND_COMPLETED");
        auditSuccess(AuditAction.REFUND_SUCCESS, savedRefund,
                "Refund completed successfully for original transaction " + original.getTransactionReference());
        return savedRefund;
    }

    public Transaction reverseTransaction(ReversalRequest request) {
        if (request == null || request.getIdempotencyKey() == null) throw new IllegalArgumentException("Reversal request is required");
        User actor = accessControlService.currentUser();
        String scopedKey = IdempotencyKeyUtil.scoped(actor.getId(), request.getIdempotencyKey());
        return executeIdempotent(
                scopedKey,
                existing -> validateExisting(existing, TransactionType.REVERSAL, null, null, null, null,
                        request.getOriginalTransactionReference()),
                () -> reverseInternal(request, scopedKey)
        );
    }

    private Transaction reverseInternal(ReversalRequest request, String scopedKey) {

        Transaction original = transactionRepository.findByTransactionReferenceForUpdate(request.getOriginalTransactionReference())
                .orElseThrow(() -> new TransactionNotFoundException("Original transaction not found"));
        validateReversal(original);

        Transaction reversal = new Transaction();
        reversal.setTransactionReference(generateTransactionReference("REV"));
        reversal.setAmount(original.getAmount());
        reversal.setCurrency(original.getCurrency());
        reversal.setIdempotencyKey(scopedKey);
        reversal.setType(TransactionType.REVERSAL);
        reversal.setStatus(TransactionStatus.INITIATED);
        reversal.setRelatedTransactionReference(original.getTransactionReference());

        if (original.getType() == TransactionType.LOAD_MONEY) {
            Long walletId = resolveLoadMoneyWalletId(original);
            reversal.setFromWalletId(walletId);
            reversal.setToWalletId(null);
            reversal = transactionRepository.save(reversal);
            Wallet wallet = walletService.debit(walletId, original.getAmount());
            ledgerService.recordEntry(reversal.getTransactionReference(), walletId, EntryType.REVERSAL_DEBIT, original.getAmount(), wallet.getBalance());
            ledgerService.recordExternalFundingCredit(reversal.getTransactionReference(), original.getAmount());
        } else {
            Map<Long, Wallet> locked = walletService.lockWalletsInOrder(original.getToWalletId(), original.getFromWalletId());
            reversal.setFromWalletId(original.getToWalletId());
            reversal.setToWalletId(original.getFromWalletId());
            reversal = transactionRepository.save(reversal);
            Wallet debited = walletService.debitLocked(locked.get(original.getToWalletId()), original.getAmount());
            ledgerService.recordEntry(reversal.getTransactionReference(), debited.getId(), EntryType.REVERSAL_DEBIT, original.getAmount(), debited.getBalance());
            Wallet credited = walletService.creditLocked(locked.get(original.getFromWalletId()), original.getAmount());
            ledgerService.recordEntry(reversal.getTransactionReference(), credited.getId(), EntryType.REVERSAL_CREDIT, original.getAmount(), credited.getBalance());
        }

        requireBalanced(reversal.getTransactionReference(), "Reversal");
        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(original);
        reversal.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(reversal);
        writeSuccessOutbox(saved, "Transaction reversed successfully", KafkaTopics.TRANSACTION_SUCCESS, "REVERSAL_SUCCESS");
        auditSuccess(AuditAction.REVERSAL_SUCCESS, saved, "Transaction reversed successfully");
        return saved;
    }

    public Transaction getTransaction(Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + id));
        accessControlService.requireTransactionAccess(tx);
        return tx;
    }

    public Page<Transaction> getWalletTransactions(Long walletId, Pageable pageable) {
        accessControlService.requireWalletAccess(walletId);
        return transactionRepository.findByFromWalletIdOrToWalletId(walletId, walletId, pageable);
    }

    public void validateRefund(RefundRequest request, Transaction original) {
        if (original == null) throw new TransactionNotFoundException("Original transaction not found");
        if (original.getStatus() != TransactionStatus.SUCCESS && original.getStatus() != TransactionStatus.PARTIALLY_REFUNDED) {
            throw new TransactionNotComplete("Only successful or partially refunded transactions can be refunded");
        }
        if (original.getType() != TransactionType.MERCHANT_PAYMENT && original.getType() != TransactionType.TRANSFER) {
            throw new InvalidTransactionException("Only merchant payment or transfer transactions can be refunded");
        }
        if (request.getRefundAmount() == null || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountMismatch("Refund amount must be greater than zero");
        }
        BigDecimal alreadyRefunded = original.getRefundedAmount() == null ? BigDecimal.ZERO : original.getRefundedAmount();
        BigDecimal remaining = original.getAmount().subtract(alreadyRefunded);
        if (request.getRefundAmount().compareTo(remaining) > 0) {
            throw new AmountMismatch("Refund amount exceeds remaining refundable amount " + remaining);
        }
    }

    private Transaction executeIdempotent(String idempotencyKey,
                                          Function<Transaction, Transaction> existingHandler,
                                          Supplier<Transaction> operation) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency key is required");
        return distributedLockService.withLock(
                "idempotency:" + idempotencyKey,
                Duration.ofSeconds(5),
                Duration.ofSeconds(45),
                () -> {
                    Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
                    if (existing.isPresent()) return existingHandler.apply(existing.get());
                    Transaction result = transactionTemplate.execute(status -> operation.get());
                    return Objects.requireNonNull(result, "Transaction returned no result");
                }
        );
    }

    private Transaction validateExisting(Transaction existing, TransactionType expectedType, Long expectedFrom, Long expectedTo,
                                         BigDecimal expectedAmount, String expectedCurrency, String expectedRelatedReference) {
        boolean matches = existing.getType() == expectedType
                && (expectedFrom == null || Objects.equals(existing.getFromWalletId(), expectedFrom))
                && (expectedTo == null || Objects.equals(existing.getToWalletId(), expectedTo))
                && (expectedAmount == null || existing.getAmount().compareTo(expectedAmount) == 0)
                && (expectedCurrency == null || expectedCurrency.equalsIgnoreCase(existing.getCurrency()))
                && (expectedRelatedReference == null || expectedRelatedReference.equals(existing.getRelatedTransactionReference()));
        if (!matches) {
            throw new InvalidTransactionException("Idempotency key was already used for a different request");
        }
        return existing;
    }

    private Transaction newTransaction(String prefix, Long fromWalletId, Long toWalletId, BigDecimal amount,
                                       String currency, String idempotencyKey, TransactionType type) {
        Transaction tx = new Transaction();
        tx.setTransactionReference(generateTransactionReference(prefix));
        tx.setFromWalletId(fromWalletId);
        tx.setToWalletId(toWalletId);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setIdempotencyKey(idempotencyKey);
        tx.setType(type);
        tx.setStatus(TransactionStatus.INITIATED);
        tx.setFailureReason(null);
        tx.setRefundedAmount(BigDecimal.ZERO);
        return tx;
    }

    private void validateTransferRequest(TransferRequest request) {
        if (request == null) throw new IllegalArgumentException("Transfer request is required");
        if (request.getFromWalletId() == null || request.getToWalletId() == null) throw new IllegalArgumentException("Both wallet ids are required");
        if (request.getFromWalletId().equals(request.getToWalletId())) throw new IllegalArgumentException("From wallet and to wallet cannot be same");
        validateCommon(request.getAmount(), request.getCurrency(), request.getIdempotencyKey());
    }

    private void validatePaymentRequest(PayRequest request) {
        if (request == null) throw new IllegalArgumentException("Payment request is required");
        if (request.getFromWalletId() == null || request.getToWalletId() == null) throw new IllegalArgumentException("Both wallet ids are required");
        if (request.getFromWalletId().equals(request.getToWalletId())) throw new IllegalArgumentException("Customer wallet and merchant wallet cannot be same");
        validateCommon(request.getAmount(), request.getCurrency(), request.getIdempotencyKey());
    }

    private void validateCommon(BigDecimal amount, String currency, String key) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Transaction amount must be greater than zero");
        CurrencyUtil.normalize(currency);
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Idempotency key is required");
    }

    private void validateReversal(Transaction original) {
        if (original == null) throw new TransactionNotFoundException("Original transaction not found");
        if (original.getStatus() != TransactionStatus.SUCCESS) {
            throw new InvalidTransactionException("Only an unreversed, unrefunded SUCCESS transaction can be reversed");
        }
        if (original.getType() != TransactionType.TRANSFER
                && original.getType() != TransactionType.MERCHANT_PAYMENT
                && original.getType() != TransactionType.LOAD_MONEY) {
            throw new InvalidTransactionException("Transaction type is not reversible");
        }
    }

    private void validateWalletCurrency(Long walletId, String currency) {
        Wallet wallet = walletService.findById(walletId);
        if (!wallet.getCurrency().equalsIgnoreCase(currency)) {
            throw new InvalidTransactionException("Currency mismatch for wallet " + walletId + ". Wallet currency is " + wallet.getCurrency());
        }
    }

    private Long resolveLoadMoneyWalletId(Transaction transaction) {
        Long walletId = transaction.getToWalletId() != null ? transaction.getToWalletId() : transaction.getFromWalletId();
        if (walletId == null) throw new InvalidTransactionException("LOAD_MONEY transaction does not contain a wallet id");
        return walletId;
    }

    private void requireBalanced(String ref, String operation) {
        if (!auditService.verifyTransaction(ref)) throw new LedgerValidationException(operation + " ledger is not balanced");
    }

    private void writeSuccessOutbox(Transaction saved, String message, String topic, String eventType) {
        outboxEventService.saveEvent(eventType, topic, createTransactionEvent(saved, message));
    }

    private TransactionEvent createTransactionEvent(Transaction transaction, String message) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionReference(transaction.getTransactionReference());
        event.setFromWalletId(transaction.getFromWalletId());
        event.setToWalletId(transaction.getToWalletId());
        event.setAmount(transaction.getAmount());
        event.setCurrency(transaction.getCurrency());
        event.setType(transaction.getType());
        event.setStatus(transaction.getStatus());
        event.setCreatedAt(transaction.getCreatedAt());
        event.setMessage(message);
        return event;
    }

    private String generateTransactionReference(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void auditSuccess(AuditAction action, Transaction tx, String description) {
        User actor = accessControlService.currentUser();
        auditLogService.logSuccess(action, actor.getId(), actor.getUsername(), actor.getRole(),
                tx.getFromWalletId(), tx.getTransactionReference(), description);
    }

    public boolean validateLedger(String transactionReference) {
        return auditService.verifyTransaction(transactionReference);
    }
}
