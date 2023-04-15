package com.jungmini.pay.service;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedissonLockService implements LockService {

    private final RedissonClient redissonClient;

    @Override
    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));

        try {
            boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                throw new PayException(ErrorCode.ACCOUNT_LOCK_FAIL);
            }
        } catch (Exception e) {
            throw new PayException(ErrorCode.ACCOUNT_LOCK_FAIL);
        }
    }

    @Override
    public void unlock(String accountNumber) {
        redissonClient.getLock(getLockKey(accountNumber));
    }

    private static String getLockKey(String accountNumber) {
        return "ACCOUNTLOCK:" + accountNumber;
    }
}
