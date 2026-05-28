package com.zz.fintrack.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret:change-me-please-change-me-please-change-me}") String secret,
            @Value("${app.jwt.expiration-ms:604800000}") long expirationMs // 7 days
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    void validateSecret() {
        String encoded = new String(key.getEncoded());
        if (encoded.startsWith("change-me")) {
            logger.warn("\u26a0\ufe0f  Using default JWT secret! Set app.jwt.secret in production.");
        }
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}


