package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void checkRateLimit(String key, int maxRequests, Duration duration) {

        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, duration);
        }

        if (count != null && count > maxRequests) {
            throw new RateLimitExceededException(
                    "Too many requests. Please try again later."
            );
        }
    }
}