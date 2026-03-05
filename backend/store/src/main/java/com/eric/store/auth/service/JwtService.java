package com.eric.store.auth.service;

import com.eric.store.common.exceptions.IllegalJsonException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
public class JwtService {
    private final SecretKey key;
    private final Duration accessExpiration;
    private static final String ISSUER = "eric-store";

    public JwtService(SecretKey key,
                      @Value("${app.jwt.access-expiration-minutes}") long accessMinutes) {
        this.key = key;
        this.accessExpiration = Duration.ofMinutes(accessMinutes);
    }

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuer(ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration)))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        if (token == null) {
            throw new IllegalJsonException("Access token is null");
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {

        try {
            Claims c = parseAndValidate(token);
            return c.getExpiration() == null || c.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String subject(String token) {
        return parseAndValidate(token).getSubject();
    }

    public List<String> roles(String token) {
        Object claim = parseAndValidate(token).get("roles");

        if (claim instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }
}
