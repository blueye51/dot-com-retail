package com.eric.store.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Component
public class JwtService {
    private final SecretKey key;
    public JwtService(SecretKey key) { this.key = key; }
    private final String ISSUER = "eric-store";

    public String generateAccessToken(String subject, Map<String, Object> extraClaims, long minutes) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuer(ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(minutes * 60)))
                .signWith(key)
                .compact();

    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public boolean isValid(String token) {
        try {
            Claims c = parseAndValidate(token);
            // if no expiration, consider valid, otherwise check date
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
