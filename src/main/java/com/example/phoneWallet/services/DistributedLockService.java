package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.DependencyUnavailableException;
import com.example.phoneWallet.Exceptions.IdempotencyInProgressException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class DistributedLockService {
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T withLock(String key, Duration wait, Duration lease, Supplier<T> work) {
        String redisKey = "lock:" + key;
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + wait.toNanos();
        boolean acquired = false;
        try {
            while (System.nanoTime() < deadline) {
                Boolean ok = redisTemplate.opsForValue().setIfAbsent(redisKey, token, lease);
                if (Boolean.TRUE.equals(ok)) {
                    acquired = true;
                    break;
                }
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IdempotencyInProgressException("Interrupted while waiting for the idempotency lock");
                }
            }
        } catch (DataAccessException ex) {
            throw new DependencyUnavailableException("Redis is unavailable; money operation was not executed", ex);
        }

        if (!acquired) {
            throw new IdempotencyInProgressException("An operation with this idempotency key is already in progress");
        }

        try {
            return work.get();
        } finally {
            try {
                redisTemplate.execute(RELEASE_SCRIPT, List.of(redisKey), token);
            } catch (DataAccessException ignored) {
                // Lease expiration still prevents a permanent lock.
            }
        }
    }
}
