package com.jungmini.pay.service;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public final class SHA256Encoder implements PasswordEncoder {

    private final String hashAlgorithm = "SHA-256";

    @Override
    public String encode(String plainText) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            messageDigest.update(plainText.getBytes());
            return byteToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("비밀번호 암호화 알고리즘 인스턴스 못 찾음");
        }
    }

    private String byteToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]); // 1바이트니까 0000 0000자리 숫자이고 가지고 있는 비트만 뽑아온다.
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    public boolean verify(String plainText, String encodedText) {
        return encodedText.equals(encode(plainText));
    }
}
