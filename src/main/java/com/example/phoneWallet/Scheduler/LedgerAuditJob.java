package com.example.phoneWallet.Scheduler;

import com.example.phoneWallet.Repository.TransactionRepository;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.services.AuditLedgerService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LedgerAuditJob {

    private final TransactionRepository transactionRepository;
    private final AuditLedgerService auditService;

    public LedgerAuditJob(TransactionRepository transactionRepository, AuditLedgerService auditService) {
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    // Runs every night at midnight to check all successful transactions
//    @Scheduled(fixedRate = 60000)
    public void runMidnightAudit() {
        System.out.println("Starting scheduled ledger integrity audit...");

        // FIX: Instead of just SUCCESS, pull all transactions or find by multiple statuses
        List<Transaction> transactionsToAudit = transactionRepository.findAll();

        int structuralFailures = 0;

        for (Transaction tx : transactionsToAudit) {
            // Skip transactions that never actually started moving money
            if (tx.getStatus() == TransactionStatus.INITIATED) {
                continue;
            }

            boolean pass = auditService.verifyTransaction(tx.getTransactionReference());
            if (!pass) {
                structuralFailures++;
            }
        }

        if (structuralFailures > 0) {
            System.err.println("AUDIT COMPLETE: " + structuralFailures + " corruption errors found!");
        } else {
            System.out.println("AUDIT COMPLETE: All database entries are perfectly balanced.");
        }
    }
}