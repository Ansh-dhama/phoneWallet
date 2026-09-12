package com.example.phoneWallet;

import com.example.phoneWallet.Repository.LedgerRepository;
import com.example.phoneWallet.services.AuditLedgerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLedgerServiceTest {

    @Mock LedgerRepository ledgerRepository;

    @Test
    void flushesPendingLedgerEntriesBeforeBalanceCheck() {
        when(ledgerRepository.sumAmountByTransactionIdAndTypes(
                eq("TX-1"),
                eq(List.of("DEBIT", "REVERSAL_DEBIT", "REFUND_DEBIT", "EXTERNAL_DEBIT"))
        )).thenReturn(new BigDecimal("8000.00"));
        when(ledgerRepository.sumAmountByTransactionIdAndTypes(
                eq("TX-1"),
                eq(List.of("CREDIT", "REVERSAL_CREDIT", "REFUND_CREDIT", "EXTERNAL_CREDIT"))
        )).thenReturn(new BigDecimal("8000.00"));

        AuditLedgerService service = new AuditLedgerService(ledgerRepository);
        assertTrue(service.verifyTransaction("TX-1"));

        InOrder inOrder = inOrder(ledgerRepository);
        inOrder.verify(ledgerRepository).flush();
        inOrder.verify(ledgerRepository).sumAmountByTransactionIdAndTypes(
                eq("TX-1"),
                eq(List.of("DEBIT", "REVERSAL_DEBIT", "REFUND_DEBIT", "EXTERNAL_DEBIT"))
        );
        inOrder.verify(ledgerRepository).sumAmountByTransactionIdAndTypes(
                eq("TX-1"),
                eq(List.of("CREDIT", "REVERSAL_CREDIT", "REFUND_CREDIT", "EXTERNAL_CREDIT"))
        );
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void rejectsUnbalancedLedger() {
        when(ledgerRepository.sumAmountByTransactionIdAndTypes(
                eq("TX-BAD"),
                eq(List.of("DEBIT", "REVERSAL_DEBIT", "REFUND_DEBIT", "EXTERNAL_DEBIT"))
        )).thenReturn(BigDecimal.ZERO);
        when(ledgerRepository.sumAmountByTransactionIdAndTypes(
                eq("TX-BAD"),
                eq(List.of("CREDIT", "REVERSAL_CREDIT", "REFUND_CREDIT", "EXTERNAL_CREDIT"))
        )).thenReturn(new BigDecimal("100.00"));

        AuditLedgerService service = new AuditLedgerService(ledgerRepository);
        assertFalse(service.verifyTransaction("TX-BAD"));
        verify(ledgerRepository).flush();
    }
}
