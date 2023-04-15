package com.jungmini.pay.service;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LockServiceTest {

    @InjectMocks
    RedissonLockService redissonLockService;

    @Mock
    RedissonClient redissonClient;

    @Mock
    RLock rLock;

    @Test
    void lock_test() throws InterruptedException {
        when(redissonClient.getLock(any()))
                .thenReturn(rLock);

        when(rLock.tryLock(anyLong(), anyLong(), any()))
                .thenReturn(true);

        redissonLockService.lock(any());
    }

    @Test
    void lock_fail_when_isLock_false() throws InterruptedException {
        when(redissonClient.getLock(any()))
                .thenReturn(rLock);

        when(rLock.tryLock(anyLong(), anyLong(), any()))
                .thenReturn(false);

        PayException payException = assertThrows(PayException.class, () ->
                redissonLockService.lock(any()));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCK_FAIL);
    }

    @Test
    void lock_fail_when_try_lock_throw_exception() throws InterruptedException {
        when(redissonClient.getLock(any()))
                .thenReturn(rLock);

        when(rLock.tryLock(anyLong(), anyLong(), any()))
                .thenThrow(new InterruptedException());

        PayException payException = assertThrows(PayException.class, () ->
                redissonLockService.lock(any()));

        assertThat(payException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCK_FAIL);
    }
}
