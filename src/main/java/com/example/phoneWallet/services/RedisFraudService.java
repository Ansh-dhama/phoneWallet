package com.example.phoneWallet.services;


import com.example.phoneWallet.Exceptions.TransactionBlock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisFraudService {
private  static final int MAX_TRANSACTIONS_PER_MINUTE = 5;

private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

private final StringRedisTemplate stringRedisTemplate;
public RedisFraudService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
}

public void checkTransactionVelocity(Long walletId){
        String key ="fraud:wallet:" + walletId + ":txn_count";
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if(count != null && count==1){
            stringRedisTemplate.expire(key,WINDOW_DURATION);
        }
        if(count!=null && count > MAX_TRANSACTIONS_PER_MINUTE){
            throw new TransactionBlock(
                    "Too many transactions. Please try again later."
            );
        }
    }
}
