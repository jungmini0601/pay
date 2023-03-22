package com.jungmini.pay.service;

import java.security.NoSuchAlgorithmException;

public interface PasswordEncoder {
    String encode(String plainText) throws NoSuchAlgorithmException;

    boolean verify(String plainText, String encodedText);
}
