package com.jungmini.pay.service;

public interface PasswordEncoder {
    String encode(String plainText);

    boolean verify(String plainText, String encodedText);

}
