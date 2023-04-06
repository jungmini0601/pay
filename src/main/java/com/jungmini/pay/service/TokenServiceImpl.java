package com.jungmini.pay.service;

import com.jungmini.pay.common.exception.ErrorCode;
import com.jungmini.pay.common.exception.PayException;
import com.jungmini.pay.common.properties.JwtProperties;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final JwtProperties jwtProperties;

    public String generateToken(final String email) {
        Key key = generateKey();
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .signWith(key)
                .setExpiration(new Date(now.getTime() + jwtProperties.getExpire()))
                .compact();
    }

    public String verifyToken(final String token) {
        try {
            Key key = generateKey();
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody().getSubject();
        } catch (SignatureException e) {
            throw new PayException(ErrorCode.TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            throw new PayException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    private Key generateKey() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        Key key = Keys.hmacShaKeyFor(bytes);
        return key;
    }
}
