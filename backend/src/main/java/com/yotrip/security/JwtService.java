package com.yotrip.security;

import com.yotrip.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    private final AppProperties appProperties;
    private final SecretKey secretKey;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String mobile) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + appProperties.getJwt().getExpirationMs());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("mobile", mobile)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
}
