package com.jungmini.pay.service;

public interface LockService {

    void lock(String accountNumber);

    void unlock(String accountNumber);

}
