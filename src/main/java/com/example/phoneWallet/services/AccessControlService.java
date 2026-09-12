package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.Repository.WalletRepository;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.Role;
import com.example.phoneWallet.enums.TransactionType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
    private final CurrentUserService currentUserService;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public AccessControlService(CurrentUserService currentUserService, WalletRepository walletRepository, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public User currentUser() {
        return currentUserService.requireCurrentUser();
    }

    public boolean isAdmin() {
        return currentUser().getRole() == Role.ADMIN;
    }

    public Wallet requireOwnedWallet(Long walletId) {
        User user = currentUser();
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new com.example.phoneWallet.Exceptions.WalletNotFoundException(walletId));
        if (!wallet.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own wallet " + walletId);
        }
        return wallet;
    }

    public Wallet requireWalletAccess(Long walletId) {
        User user = currentUser();
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new com.example.phoneWallet.Exceptions.WalletNotFoundException(walletId));
        if (user.getRole() != Role.ADMIN && !wallet.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to wallet " + walletId);
        }
        return wallet;
    }

    public Wallet requireMerchantWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new com.example.phoneWallet.Exceptions.WalletNotFoundException(walletId));
        User owner = userRepository.findById(wallet.getUserId())
                .orElseThrow(() -> new AccessDeniedException("Wallet owner does not exist"));
        if (owner.getRole() != Role.MERCHANT) {
            throw new AccessDeniedException("Destination wallet is not a verified merchant wallet");
        }
        return wallet;
    }

    public void requireTransactionAccess(Transaction transaction) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) return;
        if (ownsWallet(user.getId(), transaction.getFromWalletId()) || ownsWallet(user.getId(), transaction.getToWalletId())) return;
        throw new AccessDeniedException("You do not have access to this transaction");
    }

    public void requireRefundAccess(Transaction original) {
        User user = currentUser();
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() != Role.MERCHANT) {
            throw new AccessDeniedException("Only the receiving merchant or an administrator can issue a refund");
        }
        if (original.getType() != TransactionType.MERCHANT_PAYMENT) {
            throw new AccessDeniedException("Merchants can only refund their own merchant payments");
        }
        if (!ownsWallet(user.getId(), original.getToWalletId())) {
            throw new AccessDeniedException("This payment does not belong to the current merchant");
        }
    }

    private boolean ownsWallet(Long userId, Long walletId) {
        if (walletId == null) return false;
        return walletRepository.findById(walletId).map(w -> w.getUserId().equals(userId)).orElse(false);
    }
}
