package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.DependencyUnavailableException;
import com.example.phoneWallet.Exceptions.TransactionBlock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisFraudService {
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final StringRedisTemplate redis;

    public RedisFraudService(StringRedisTemplate redis) { this.redis = redis; }

    public void checkTransactionVelocity(Long walletId) {
        try {
            String key = "fraud:wallet:" + walletId + ":txn_count";
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1) redis.expire(key, WINDOW);
            if (count != null && count > MAX_TRANSACTIONS_PER_MINUTE) {
                throw new TransactionBlock("Too many wallet transactions. Please try again later.");
            }
        } catch (TransactionBlock ex) {
            throw ex;
        } catch (DataAccessException ex) {
            // Money movement is fail-closed when fraud controls are unavailable.
            throw new DependencyUnavailableException("Fraud-control service is temporarily unavailable", ex);
        }
    }
}
