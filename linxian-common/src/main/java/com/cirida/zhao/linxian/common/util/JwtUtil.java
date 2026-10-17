package com.cirida.zhao.linxian.common.util;

import com.cirida.zhao.linxian.common.constant.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具（静态方法，签名与解析共用同一套逻辑）。
 * <p>用户服务负责签发，网关负责校验。HS256 要求密钥长度 >= 32 字节。</p>
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    public static String createToken(String secret, long ttlMillis, Long userId, String username) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim(Constants.JWT_CLAIM_USER_ID, userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMillis))
                .signWith(key)
                .compact();
    }

    public static Claims parseToken(String secret, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
