package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.LedgerRepository;
import com.example.phoneWallet.enums.EntryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class AuditLedgerService {

    private final LedgerRepository ledgerRepository;

    public AuditLedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    private final List<String> debitTypes = List.of(
            EntryType.DEBIT.name(),
            EntryType.REVERSAL_DEBIT.name(),
            EntryType.REFUND_DEBIT.name(),
            EntryType.EXTERNAL_DEBIT.name()
    );

    private final List<String> creditTypes = List.of(
            EntryType.CREDIT.name(),
            EntryType.REVERSAL_CREDIT.name(),
            EntryType.REFUND_CREDIT.name(),
            EntryType.EXTERNAL_CREDIT.name()
    );

    public boolean verifyTransaction(String transactionRef) {

        // Ensure ledger entries written in the current transaction are visible to the
        // native SUM queries below. This prevents false OUT_OF_BALANCE results on
        // transfer/pay/refund/top-up before the transaction commits.
        ledgerRepository.flush();

        BigDecimal debitAmount = ledgerRepository.sumAmountByTransactionIdAndTypes(
                transactionRef,
                debitTypes
        );

        BigDecimal creditAmount = ledgerRepository.sumAmountByTransactionIdAndTypes(
                transactionRef,
                creditTypes
        );

        if (debitAmount == null) {
            debitAmount = BigDecimal.ZERO;
        }

        if (creditAmount == null) {
            creditAmount = BigDecimal.ZERO;
        }

        boolean isBalanced = debitAmount.compareTo(creditAmount) == 0;

        if (!isBalanced) {
            log.error(
                    "CRITICAL LEDGER ERROR: Transaction Ref {} is OUT OF BALANCE. Debit total: {}, Credit total: {}",
                    transactionRef,
                    debitAmount,
                    creditAmount
            );
        } else {
            log.info(
                    "Ledger verified successfully. Transaction Ref: {}, Amount: {}",
                    transactionRef,
                    debitAmount
            );
        }

        return isBalanced;
    }
}