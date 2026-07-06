package com.example.phoneWallet;

import com.example.phoneWallet.dto.TransferRequest;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.WalletStatus;
import com.example.phoneWallet.Repository.WalletRepository;
import com.example.phoneWallet.services.TransactionService;
import com.example.phoneWallet.services.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WalletConcurrencyTest {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private WalletService walletService;

	@Autowired
	private WalletRepository walletRepository;

	@Test
	void shouldAllowOnlyOneDebitWhenTwoRequestsComeAtSameTime() throws Exception {

		System.out.println("========== DOUBLE DEBIT CONCURRENCY TEST START ==========");

		Wallet fromWallet = new Wallet();
		fromWallet.setUserId(101L);
		fromWallet.setWalletNumber("W-" + UUID.randomUUID());
		fromWallet.setBalance(new BigDecimal("1000"));
		fromWallet.setCurrency("INR");
		fromWallet.setStatus(WalletStatus.ACTIVE);
		fromWallet = walletRepository.save(fromWallet);

		Wallet toWallet1 = new Wallet();
		toWallet1.setUserId(102L);
		toWallet1.setWalletNumber("W-" + UUID.randomUUID());
		toWallet1.setBalance(BigDecimal.ZERO);
		toWallet1.setCurrency("INR");
		toWallet1.setStatus(WalletStatus.ACTIVE);
		toWallet1 = walletRepository.save(toWallet1);

		Wallet toWallet2 = new Wallet();
		toWallet2.setUserId(103L);
		toWallet2.setWalletNumber("W-" + UUID.randomUUID());
		toWallet2.setBalance(BigDecimal.ZERO);
		toWallet2.setCurrency("INR");
		toWallet2.setStatus(WalletStatus.ACTIVE);
		toWallet2 = walletRepository.save(toWallet2);

		System.out.println("Sender Wallet ID      : " + fromWallet.getId());
		System.out.println("Receiver Wallet 1 ID  : " + toWallet1.getId());
		System.out.println("Receiver Wallet 2 ID  : " + toWallet2.getId());
		System.out.println("Initial Sender Balance: " + fromWallet.getBalance());
		System.out.println("Request 1 Debit Amount: 800");
		System.out.println("Request 2 Debit Amount: 800");
		System.out.println("Expected Result       : Only one request should succeed");
		System.out.println("---------------------------------------------------------");

		TransferRequest request1 = new TransferRequest();
		request1.setFromWalletId(fromWallet.getId());
		request1.setToWalletId(toWallet1.getId());
		request1.setAmount(new BigDecimal("800"));
		request1.setCurrency("INR");
		request1.setIdempotencyKey("CONCURRENCY-" + UUID.randomUUID());

		TransferRequest request2 = new TransferRequest();
		request2.setFromWalletId(fromWallet.getId());
		request2.setToWalletId(toWallet2.getId());
		request2.setAmount(new BigDecimal("800"));
		request2.setCurrency("INR");
		request2.setIdempotencyKey("CONCURRENCY-" + UUID.randomUUID());

		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch latch = new CountDownLatch(1);

		Callable<Boolean> task1 = () -> {
			latch.await();
			System.out.println("Thread-1 started. Trying to debit 800 from wallet " + request1.getFromWalletId());

			try {
				Transaction transaction = transactionService.processTransaction(request1);
				System.out.println("Thread-1 transaction reference: " + transaction.getTransactionReference());
				System.out.println("Thread-1 transaction status   : " + transaction.getStatus());
				return transaction.getStatus() == TransactionStatus.SUCCESS;
			} catch (Exception ex) {
				System.out.println("Thread-1 failed");
				System.out.println("Thread-1 error: " + ex.getMessage());
				return false;
			}
		};

		Callable<Boolean> task2 = () -> {
			latch.await();
			System.out.println("Thread-2 started. Trying to debit 800 from wallet " + request2.getFromWalletId());

			try {
				Transaction transaction = transactionService.processTransaction(request2);
				System.out.println("Thread-2 transaction reference: " + transaction.getTransactionReference());
				System.out.println("Thread-2 transaction status   : " + transaction.getStatus());
				return transaction.getStatus() == TransactionStatus.SUCCESS;
			} catch (Exception ex) {
				System.out.println("Thread-2 failed");
				System.out.println("Thread-2 error: " + ex.getMessage());
				return false;
			}
		};

		Future<Boolean> future1 = executorService.submit(task1);
		Future<Boolean> future2 = executorService.submit(task2);

		System.out.println("Both threads are ready. Releasing latch now...");
		latch.countDown();

		boolean result1 = future1.get();
		boolean result2 = future2.get();

		executorService.shutdown();

		long successCount = Stream.of(result1, result2)
				.filter(Boolean::booleanValue)
				.count();

		long failedCount = 2 - successCount;

		BigDecimal finalSenderBalance = walletService.getBalance(fromWallet.getId());
		BigDecimal receiver1Balance = walletService.getBalance(toWallet1.getId());
		BigDecimal receiver2Balance = walletService.getBalance(toWallet2.getId());

		System.out.println("---------------------------------------------------------");
		System.out.println("Thread-1 success result : " + result1);
		System.out.println("Thread-2 success result : " + result2);
		System.out.println("Success Count           : " + successCount);
		System.out.println("Failed Count            : " + failedCount);
		System.out.println("Final Sender Balance    : " + finalSenderBalance);
		System.out.println("Receiver 1 Balance      : " + receiver1Balance);
		System.out.println("Receiver 2 Balance      : " + receiver2Balance);
		System.out.println("Expected Success Count  : 1");
		System.out.println("Expected Final Balance  : 200");
		System.out.println("========== DOUBLE DEBIT CONCURRENCY TEST END ==========");

		assertEquals(1, successCount);
		assertEquals(0, finalSenderBalance.compareTo(new BigDecimal("200")));
	}
}