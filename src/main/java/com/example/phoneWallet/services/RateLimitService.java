package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.DependencyUnavailableException;
import com.example.phoneWallet.Exceptions.RateLimitExceededException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private final StringRedisTemplate redis;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void checkRateLimit(String key, int maxRequests, Duration duration, boolean failClosed) {
        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1) redis.expire(key, duration);
            if (count != null && count > maxRequests) {
                throw new RateLimitExceededException("Too many requests. Please try again later.");
            }
        } catch (RateLimitExceededException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            if (failClosed) {
                throw new DependencyUnavailableException("Rate-limit service is temporarily unavailable", ex);
            }
            // Non-sensitive read operations deliberately fail open.
        }
    }
}
