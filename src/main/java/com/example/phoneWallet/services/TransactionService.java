package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.*;
import com.example.phoneWallet.Repository.TransactionRepository;
import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.EntryType;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import com.example.phoneWallet.kafka.KafkaTopics;
import com.example.phoneWallet.kafka.TransactionEventProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final AuditLedgerService auditService;
    private final RiskService riskService;
    private final AuditLogService auditLogService;
    private final OutboxEventService outboxEventService;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletService walletService,
            LedgerService ledgerService,
            AuditLedgerService auditService,
            RiskService riskService,
            AuditLogService auditLogService,
            OutboxEventService outboxEventService
    ) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.riskService = riskService;
        this.auditLogService = auditLogService;
        this.outboxEventService = outboxEventService;
    }

    /*
     * Wallet-to-wallet transfer.
     * FULL ROLLBACK DESIGN:
     * If debit, credit, or ledger entry fails, everything rolls back.
     */
    @Transactional
    public Transaction processTransaction(TransferRequest request) {
        Transaction transaction = new Transaction();

       try {
            validateTransferRequest(request);

            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

            if (existingTransaction.isPresent()) {
                return existingTransaction.get();
            }

            transaction.setTransactionReference(generateTransactionReference("TXN"));
            transaction.setFromWalletId(request.getFromWalletId());
            transaction.setToWalletId(request.getToWalletId());
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setIdempotencyKey(request.getIdempotencyKey());
            transaction.setType(TransactionType.TRANSFER);
            transaction.setStatus(TransactionStatus.INITIATED);

            transaction = transactionRepository.save(transaction);
            riskService.validateRisk(request.getFromWalletId(), request.getAmount());

            Wallet wallet = walletService.debit(request.getFromWalletId(), request.getAmount());


            ledgerService.recordEntry(
                    transaction.getTransactionReference(),
                    request.getFromWalletId(),
                    EntryType.DEBIT,
                    request.getAmount(),
                    wallet.getBalance()
            );

            Wallet wallet2 = walletService.credit(request.getToWalletId(), request.getAmount());


            ledgerService.recordEntry(
                    transaction.getTransactionReference(),
                    request.getToWalletId(),
                    EntryType.CREDIT,
                    request.getAmount(),
                    wallet2.getBalance()
            );
            boolean ledgerBalanced = validateLedger(transaction.getTransactionReference());

            if (!ledgerBalanced) {
                throw new LedgerValidationException("Ledger debit and credit are not balanced");
            }

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setFailureReason(null);
            Transaction savedTransaction = transactionRepository.save(transaction);

           TransactionEvent event = createTransactionEvent(
                   savedTransaction,
                   "Transaction completed successfully"
           );

           outboxEventService.saveEvent(
                   "TRANSACTION_SUCCESS",
                   KafkaTopics.TRANSACTION_SUCCESS,
                   event
           );
            auditLogService.logSuccess(AuditAction.TRANSFER_SUCCESS,null,null,null,request.getFromWalletId(),
                    savedTransaction.getTransactionReference(),"Wallet transfer completed successfully");
            return savedTransaction;
        }catch (Exception e) {
           auditLogService.logFailure(AuditAction.TRANSFER_FAILED ,null,null,null,request.getFromWalletId(),
                   transaction != null ? transaction.getTransactionReference() : null,
                   "Wallet transfer failed",
                   e.getMessage()
                   );
           throw e;
       }

    }@Transactional
    public Transaction payTransaction(PayRequest request) {
        Transaction transaction = new Transaction();

        try {
            validatePaymentRequest(request);

            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

            if (existingTransaction.isPresent()) {
                return existingTransaction.get();
            }

            transaction.setTransactionReference(generateTransactionReference("PAY"));
            transaction.setFromWalletId(request.getFromWalletId());
            transaction.setToWalletId(request.getToWalletId());
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setIdempotencyKey(request.getIdempotencyKey());
            transaction.setType(TransactionType.MERCHANT_PAYMENT);
            transaction.setStatus(TransactionStatus.PENDING);

            transaction = transactionRepository.save(transaction);

            riskService.validateRisk(request.getFromWalletId(), request.getAmount());

            Wallet customerWallet = walletService.debit(
                    request.getFromWalletId(),
                    request.getAmount()
            );

            ledgerService.recordEntry(
                    transaction.getTransactionReference(),
                    request.getFromWalletId(),
                    EntryType.DEBIT,
                    request.getAmount(),
                    customerWallet.getBalance()
            );

            Wallet merchantWallet = walletService.credit(
                    request.getToWalletId(),
                    request.getAmount()
            );

            ledgerService.recordEntry(
                    transaction.getTransactionReference(),
                    request.getToWalletId(),
                    EntryType.CREDIT,
                    request.getAmount(),
                    merchantWallet.getBalance()
            );

            boolean ledgerBalanced = validateLedger(transaction.getTransactionReference());

            if (!ledgerBalanced) {
                throw new LedgerValidationException("Ledger debit and credit are not balanced");
            }

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setFailureReason(null);

            Transaction savedTransaction = transactionRepository.save(transaction);

            TransactionEvent event = createTransactionEvent(
                    savedTransaction,
                    "Merchant payment completed successfully"
            );

            outboxEventService.saveEvent(
                    "TRANSACTION_SUCCESS",
                    KafkaTopics.TRANSACTION_SUCCESS,
                    event
            );

            auditLogService.logSuccess(
                    AuditAction.MERCHANT_PAYMENT_SUCCESS,
                    null,
                    null,
                    null,
                    request.getFromWalletId(),
                    savedTransaction.getTransactionReference(),
                    "Merchant payment completed successfully"
            );

            return savedTransaction;

        } catch (Exception e) {
            auditLogService.logFailure(
                    AuditAction.MERCHANT_PAYMENT_FAILED,
                    null,
                    null,
                    null,
                    request.getFromWalletId(),
                    transaction != null ? transaction.getTransactionReference() : null,
                    "Merchant payment failed",
                    e.getMessage()
            );
            throw e;
        }
    }
    @Transactional
    public Transaction loadMoney(LoadMoneyRequest request,Long walletId) {
        Transaction transaction = new Transaction();
      try  {
            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

            if (existingTransaction.isPresent()) {
                return existingTransaction.get();
            }

            transaction.setTransactionReference(generateTransactionReference("TXN"));
            transaction.setFromWalletId(walletId);
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setIdempotencyKey(request.getIdempotencyKey());
            transaction.setType(TransactionType.LOAD_MONEY);
            transaction.setStatus(TransactionStatus.INITIATED);
            transaction.setFailureReason(request.getRemarks());

            transaction = transactionRepository.save(transaction);

            Wallet wallet = walletService.credit(walletId, request.getAmount());
            ledgerService.recordEntry(transaction.getTransactionReference(), walletId, EntryType.CREDIT, request.getAmount(), wallet.getBalance());

            boolean ledgerBalanced = validateLedger(transaction.getTransactionReference());

            if (!ledgerBalanced) {
                throw new LedgerValidationException("Ledger debit and credit are not balanced");
            }

            transaction.setStatus(TransactionStatus.SUCCESS);

          Transaction savedTransaction = transactionRepository.save(transaction);

          TransactionEvent event = createTransactionEvent(
                  savedTransaction,
                  "Money load completed successfully"
          );

          outboxEventService.saveEvent(
                  "TRANSACTION_SUCCESS",
                  KafkaTopics.TRANSACTION_SUCCESS,
                  event
          );          auditLogService.logSuccess(AuditAction.LOAD_MONEY_SUCCESS,null,null,null,null,
                  savedTransaction.getTransactionReference(),"Money load completed successfully");

          return savedTransaction;
      }catch (Exception e) {
          auditLogService.logFailure(AuditAction.LOAD_MONEY_FAILED ,null,null,null,null,
                  transaction != null ? transaction.getTransactionReference() : null,
                  "Money load failed",
                  e.getMessage()
          );
          throw e;
      }
    }

    @Transactional
    public Transaction refund(RefundRequest request) {
        Transaction refunded = new Transaction();

       try {
            Optional<Transaction> existingTransaction =
                    transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

            if (existingTransaction.isPresent()) {
                return existingTransaction.get();
            }

            Transaction transaction =
                    transactionRepository.findByTransactionReference(
                            request.getOriginalTransactionReference()
                    ).orElseThrow(() -> new TransactionNotFoundException("transaction reference not found"));

            validateRefund(request, transaction);

            refunded.setTransactionReference(generateTransactionReference("REFUND"));
            refunded.setFromWalletId(transaction.getToWalletId());   // merchant
            refunded.setToWalletId(transaction.getFromWalletId());   // customer
            refunded.setAmount(request.getRefundAmount());
            refunded.setCurrency(transaction.getCurrency());
            refunded.setIdempotencyKey(request.getIdempotencyKey());
            refunded.setType(TransactionType.REFUND);
            refunded.setStatus(TransactionStatus.PENDING);
            refunded.setFailureReason(null);

            refunded = transactionRepository.save(refunded);
            riskService.validateRisk(transaction.getToWalletId(), request.getRefundAmount());
            Wallet merchantWallet = walletService.debit(
                    transaction.getToWalletId(),
                    request.getRefundAmount()
            );

            ledgerService.recordEntry(
                    refunded.getTransactionReference(),
                    transaction.getToWalletId(),
                    EntryType.REFUND_DEBIT,
                    request.getRefundAmount(),
                    merchantWallet.getBalance()
            );

            Wallet customerWallet = walletService.credit(
                    transaction.getFromWalletId(),
                    request.getRefundAmount()
            );

            ledgerService.recordEntry(
                    refunded.getTransactionReference(),
                    transaction.getFromWalletId(),
                    EntryType.REFUND_CREDIT,
                    request.getRefundAmount(),
                    customerWallet.getBalance()
            );

            boolean ledgerBalanced = validateLedger(refunded.getTransactionReference());

            if (!ledgerBalanced) {
                throw new LedgerValidationException("Refund ledger debit and credit are not balanced");
            }

            transaction.setStatus(TransactionStatus.REFUNDED);
            transaction.setFailureReason(request.getRefundReason());
            transactionRepository.save(transaction);

            refunded.setStatus(TransactionStatus.SUCCESS);
            refunded.setFailureReason(null);

            Transaction savedRefund = transactionRepository.save(refunded);

           TransactionEvent event = createTransactionEvent(
                   savedRefund,
                   "Refund completed successfully"
           );

           outboxEventService.saveEvent(
                   "REFUND_COMPLETED",
                   KafkaTopics.REFUND_COMPLETED,
                   event
           );            auditLogService.logSuccess(AuditAction.REFUND_SUCCESS , null,null,null,refunded.getFromWalletId() ,
                    refunded != null ? refunded.getTransactionReference() : null, "Refund completed successfully");
            return savedRefund;
        }   catch (Exception e) {
           auditLogService.logFailure(
           AuditAction.REFUND_FAILED,null,null,null,null, refunded != null ? refunded.getTransactionReference() : null ,
                   "Refund failed ",e.getMessage());
           throw e;
       }

    }
    @Transactional
    public Transaction reverseTransaction(ReversalRequest request) {

        Transaction reversalTransaction = null;

        try {
            Optional<Transaction> existingReversal =
                    transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

            if (existingReversal.isPresent()) {
                return existingReversal.get();
            }

            Transaction originalTransaction =
                    transactionRepository.findByTransactionReference(
                            request.getOriginalTransactionReference()
                    ).orElseThrow(() ->
                            new InvalidTransactionException("Original transaction not found")
                    );

            validateReversal(originalTransaction);

            reversalTransaction = new Transaction();
            reversalTransaction.setTransactionReference(generateTransactionReference("REV"));
            reversalTransaction.setFromWalletId(originalTransaction.getToWalletId());
            reversalTransaction.setToWalletId(originalTransaction.getFromWalletId());
            reversalTransaction.setAmount(originalTransaction.getAmount());
            reversalTransaction.setCurrency(originalTransaction.getCurrency());
            reversalTransaction.setType(TransactionType.REVERSAL);
            reversalTransaction.setStatus(TransactionStatus.PENDING);
            reversalTransaction.setIdempotencyKey(request.getIdempotencyKey());
            reversalTransaction.setFailureReason(null);

            reversalTransaction = transactionRepository.save(reversalTransaction);

            if (originalTransaction.getType() == TransactionType.LOAD_MONEY) {

                Wallet walletAfterDebit = walletService.debit(
                        originalTransaction.getToWalletId(),
                        originalTransaction.getAmount()
                );

                ledgerService.recordEntry(
                        reversalTransaction.getTransactionReference(),
                        originalTransaction.getToWalletId(),
                        EntryType.REVERSAL_DEBIT,
                        originalTransaction.getAmount(),
                        walletAfterDebit.getBalance()
                );

            } else {

                Wallet debitedWallet = walletService.debit(
                        originalTransaction.getToWalletId(),
                        originalTransaction.getAmount()
                );

                ledgerService.recordEntry(
                        reversalTransaction.getTransactionReference(),
                        originalTransaction.getToWalletId(),
                        EntryType.REVERSAL_DEBIT,
                        originalTransaction.getAmount(),
                        debitedWallet.getBalance()
                );

                Wallet creditedWallet = walletService.credit(
                        originalTransaction.getFromWalletId(),
                        originalTransaction.getAmount()
                );

                ledgerService.recordEntry(
                        reversalTransaction.getTransactionReference(),
                        originalTransaction.getFromWalletId(),
                        EntryType.REVERSAL_CREDIT,
                        originalTransaction.getAmount(),
                        creditedWallet.getBalance()
                );

                boolean ledgerBalanced = validateLedger(
                        reversalTransaction.getTransactionReference()
                );

                if (!ledgerBalanced) {
                    throw new LedgerValidationException("Reversal ledger debit and credit are not balanced");
                }
            }

            originalTransaction.setStatus(TransactionStatus.REVERSED);
            originalTransaction.setFailureReason(request.getReason());
            transactionRepository.save(originalTransaction);

            reversalTransaction.setStatus(TransactionStatus.SUCCESS);
            reversalTransaction.setFailureReason(null);

            Transaction savedReversal = transactionRepository.save(reversalTransaction);
            TransactionEvent event = createTransactionEvent(
                    savedReversal,
                    "Transaction reversed successfully"
            );

            outboxEventService.saveEvent(
                    "REVERSAL_SUCCESS",
                    KafkaTopics.TRANSACTION_SUCCESS,
                    event
            );
            auditLogService.logSuccess(
                    AuditAction.REVERSAL_SUCCESS,
                    null,
                    null,
                    null,
                    savedReversal.getFromWalletId(),
                    savedReversal.getTransactionReference(),
                    "Transaction reversed successfully. Original reference: "
                            + originalTransaction.getTransactionReference()
            );

            return savedReversal;

        } catch (Exception ex) {

            auditLogService.logFailure(
                    AuditAction.REVERSAL_FAILED,
                    null,
                    null,
                    null,
                    null,
                    reversalTransaction != null
                            ? reversalTransaction.getTransactionReference()
                            : request.getOriginalTransactionReference(),
                    "Transaction reversal failed",
                    ex.getMessage()
            );

            throw ex;
        }
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
    public void validateRefund(RefundRequest request, Transaction originalTransaction) {
        if (originalTransaction == null) {
            throw new TransactionNotFoundException("Original transaction not found");
        }

        if (originalTransaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new TransactionNotComplete("Only SUCCESS transaction can be refunded");
        }

        if (originalTransaction.getType() != TransactionType.MERCHANT_PAYMENT
                && originalTransaction.getType() != TransactionType.TRANSFER) {
            throw new InvalidTransactionException("Only payment or transfer transaction can be refunded");
        }

        if (request.getRefundAmount() == null ||
                request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountMismatch("Refund amount must be greater than zero");
        }

        if (request.getRefundAmount().compareTo(originalTransaction.getAmount()) > 0) {
            throw new AmountMismatch("Refund amount cannot be greater than original transaction amount");
        }
    }
    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
    }

    public List<Transaction> getWalletTransactions(Long walletId) {
        walletService.findById(walletId);
        return transactionRepository.findByFromWalletIdOrToWalletId(walletId, walletId);
    }


    private void validateTransferRequest(TransferRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Transfer request is required");
        }

        if (request.getFromWalletId() == null) {
            throw new IllegalArgumentException("From wallet id is required");
        }

        if (request.getToWalletId() == null) {
            throw new IllegalArgumentException("To wallet id is required");
        }

        if (request.getFromWalletId().equals(request.getToWalletId())) {
            throw new IllegalArgumentException("From wallet and to wallet cannot be same");
        }

        validateCommonTransactionFields(
                request.getAmount(),
                request.getCurrency(),
                request.getIdempotencyKey()
        );
    }

    private void validatePaymentRequest(PayRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }

        if (request.getFromWalletId() == null) {
            throw new IllegalArgumentException("Customer wallet id is required");
        }

        if (request.getToWalletId() == null) {
            throw new IllegalArgumentException("Merchant wallet id is required");
        }

        if (request.getFromWalletId().equals(request.getToWalletId())) {
            throw new IllegalArgumentException("Customer wallet and merchant wallet cannot be same");
        }

        validateCommonTransactionFields(
                request.getAmount(),
                request.getCurrency(),
                request.getIdempotencyKey()
        );
    }

    private void validateCommonTransactionFields(
            BigDecimal amount,
            String currency,
            String idempotencyKey
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
    }
    private void validateReversal(Transaction originalTransaction) {

        if (originalTransaction == null) {
            throw new TransactionNotFoundException("Original transaction not found");
        }

        if (originalTransaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new InvalidTransactionException("Only SUCCESS transaction can be reversed");
        }

        if (originalTransaction.getType() != TransactionType.TRANSFER
                && originalTransaction.getType() != TransactionType.MERCHANT_PAYMENT
                && originalTransaction.getType() != TransactionType.LOAD_MONEY) {
            throw new InvalidTransactionException("Only TRANSFER, MERCHANT_PAYMENT or LOAD_MONEY can be reversed");
        }
    }

    private String generateTransactionReference(String prefix) {
        return prefix + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
    public boolean validateLedger(String transactionReference) {
       return auditService.verifyTransaction(transactionReference);
    }
}