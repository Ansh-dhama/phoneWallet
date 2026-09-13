package com.example.phoneWallet;

import com.example.phoneWallet.Repository.TopUpIntentRepository;
import com.example.phoneWallet.Repository.TransactionRepository;
import com.example.phoneWallet.dto.RazorpayPaymentVerificationRequest;
import com.example.phoneWallet.dto.TopUpIntentRequest;
import com.example.phoneWallet.dto.TopUpIntentResponse;
import com.example.phoneWallet.entity.TopUpIntent;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.Role;
import com.example.phoneWallet.enums.TopUpStatus;
import com.example.phoneWallet.enums.WalletStatus;
import com.example.phoneWallet.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies that creating a Razorpay order never changes wallet balance and that
 * wallet credit happens only after the Razorpay payment verification path succeeds.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceLoadMoneyTest {

    @Mock TopUpIntentRepository topUpRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock WalletService walletService;
    @Mock AccessControlService accessControlService;
    @Mock LedgerService ledgerService;
    @Mock AuditLedgerService auditLedgerService;
    @Mock OutboxEventService outboxEventService;
    @Mock AuditLogService auditLogService;
    @Mock RazorpayPaymentService razorpayPaymentService;

    TopUpService topUpService;
    User user;
    Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        user.setUsername("walletuser");
        user.setRole(Role.USER);

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUserId(user.getId());
        wallet.setWalletNumber("W-1");
        wallet.setCurrency("INR");
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setBalance(BigDecimal.ZERO);

        topUpService = new TopUpService(
                topUpRepository, transactionRepository, walletService, accessControlService,
                ledgerService, auditLedgerService, outboxEventService, auditLogService,
                razorpayPaymentService, new BigDecimal("50000")
        );
    }

    @Test
    void initiatingTopUpCreatesRazorpayOrderButDoesNotChangeWalletBalance() {
        when(accessControlService.currentUser()).thenReturn(user);
        when(accessControlService.requireOwnedWallet(1L)).thenReturn(wallet);
        when(topUpRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(razorpayPaymentService.createOrder(new BigDecimal("8000.00"), "INR", 1L, 10L))
                .thenReturn("order_TEST_1");
        when(topUpRepository.save(any(TopUpIntent.class))).thenAnswer(inv -> {
            TopUpIntent intent = inv.getArgument(0);
            intent.setId(99L);
            return intent;
        });

        TopUpIntentResponse response = topUpService.initiate(
                1L, new TopUpIntentRequest(new BigDecimal("8000.00"), "INR", "client-key-1")
        );

        assertEquals(TopUpStatus.PENDING, response.status());
        assertEquals("order_TEST_1", response.providerOrderId());
        assertEquals(0, wallet.getBalance().compareTo(BigDecimal.ZERO));
        verify(walletService, never()).credit(anyLong(), any());
        verifyNoInteractions(ledgerService);
    }

    @Test
    void verifiedRazorpaySettlementCreditsOnceAndCreatesBalancedLedger() {
        TopUpIntent intent = new TopUpIntent();
        intent.setId(99L);
        intent.setUserId(user.getId());
        intent.setWalletId(wallet.getId());
        intent.setAmount(new BigDecimal("8000.00"));
        intent.setCurrency("INR");
        intent.setIdempotencyKey("scoped-key");
        intent.setProviderOrderId("order_TEST_1");
        intent.setStatus(TopUpStatus.PENDING);

        RazorpayPaymentVerificationRequest request = new RazorpayPaymentVerificationRequest(
                "pay_TEST_1", "order_TEST_1", "signature"
        );

        when(topUpRepository.findByIdForUpdate(99L)).thenReturn(Optional.of(intent));
        when(accessControlService.requireOwnedWallet(1L)).thenReturn(wallet);
        when(razorpayPaymentService.verifyCapturedPayment(
                "order_TEST_1", new BigDecimal("8000.00"), "INR", request))
                .thenReturn("pay_TEST_1");
        when(transactionRepository.findByIdempotencyKey("topup-settlement:order_TEST_1"))
                .thenReturn(Optional.empty());
        when(walletService.credit(eq(1L), eq(new BigDecimal("8000.00")))).thenAnswer(inv -> {
            wallet.setBalance(wallet.getBalance().add(inv.getArgument(1)));
            return wallet;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(topUpRepository.save(any(TopUpIntent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLedgerService.verifyTransaction(anyString())).thenReturn(true);

        TopUpIntentResponse response = topUpService.verifyRazorpayPayment(99L, request);

        assertEquals(TopUpStatus.COMPLETED, response.status());
        assertEquals("pay_TEST_1", response.providerPaymentId());
        assertEquals(0, wallet.getBalance().compareTo(new BigDecimal("8000.00")));
        verify(ledgerService).recordExternalFundingDebit(anyString(), eq(new BigDecimal("8000.00")));
        verify(ledgerService).recordEntry(anyString(), eq(1L), any(), eq(new BigDecimal("8000.00")), eq(new BigDecimal("8000.00")));
        verify(auditLedgerService).verifyTransaction(anyString());
        verify(outboxEventService).saveEvent(eq("TRANSACTION_SUCCESS"), anyString(), any());
    }
}
