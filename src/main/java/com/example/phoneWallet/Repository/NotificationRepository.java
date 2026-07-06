package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByWalletId(Long walletId);

    List<Notification> findByTransactionReference(String transactionReference);
}