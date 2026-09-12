package com.example.phoneWallet.Scheduler;

import com.example.phoneWallet.Repository.TransactionRepository;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.services.AuditLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "wallet.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class LedgerAuditJob {
    private static final Logger log = LoggerFactory.getLogger(LedgerAuditJob.class);
    private final TransactionRepository transactionRepository;
    private final AuditLedgerService auditService;

    public LedgerAuditJob(TransactionRepository transactionRepository, AuditLedgerService auditService) {
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    @Scheduled(cron = "${wallet.ledger.audit-cron:0 0 2 * * *}")
    public void runScheduledAudit() {
        int pageNo = 0;
        int failures = 0;
        Page<Transaction> page;
        do {
            page = transactionRepository.findAll(PageRequest.of(pageNo++, 500));
            for (Transaction tx : page.getContent()) {
                if (tx.getStatus() == TransactionStatus.INITIATED || tx.getStatus() == TransactionStatus.PENDING) continue;
                if (!auditService.verifyTransaction(tx.getTransactionReference())) failures++;
            }
        } while (page.hasNext());

        if (failures > 0) log.error("Scheduled ledger reconciliation found {} unbalanced transactions", failures);
        else log.info("Scheduled ledger reconciliation completed successfully");
    }
}
