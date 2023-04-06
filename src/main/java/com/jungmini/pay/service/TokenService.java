package com.jungmini.pay.service;

public interface TokenService {

    String generateToken(String email);

    String verifyToken(String token);

}
