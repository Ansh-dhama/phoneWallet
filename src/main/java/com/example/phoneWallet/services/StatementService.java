package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.LedgerRepository;
import com.example.phoneWallet.dto.PageResponse;
import com.example.phoneWallet.dto.StatementResponse;
import com.example.phoneWallet.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatementService {
    private final LedgerRepository ledgerRepository;
    private final AccessControlService accessControlService;

    public StatementService(LedgerRepository ledgerRepository, AccessControlService accessControlService) {
        this.ledgerRepository = ledgerRepository;
        this.accessControlService = accessControlService;
    }

    public PageResponse<StatementResponse> getStatementsByWalletId(Long walletId, Pageable pageable) {
        accessControlService.requireWalletAccess(walletId);
        Page<StatementResponse> page = ledgerRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable)
                .map(this::map);
        return PageResponse.from(page);
    }

    public PageResponse<StatementResponse> getStatementByDateRange(Long walletId, LocalDateTime startDate,
                                                                   LocalDateTime endDate, Pageable pageable) {
        accessControlService.requireWalletAccess(walletId);
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("startDate must be before endDate");
        Page<StatementResponse> page = ledgerRepository
                .findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(walletId, startDate, endDate, pageable)
                .map(this::map);
        return PageResponse.from(page);
    }

    private StatementResponse map(LedgerEntry e) {
        StatementResponse r = new StatementResponse();
        r.setTransactionRef(e.getTransactionId());
        r.setWalletId(e.getWalletId());
        r.setEntryType(e.getEntryType());
        r.setAmount(e.getAmount());
        r.setBalanceAfterTransaction(e.getBalanceAfterTransaction());
        r.setTimestamp(e.getCreatedAt());
        return r;
    }
}
