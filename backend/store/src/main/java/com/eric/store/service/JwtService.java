package com.eric.store.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        Claims c = parseAndValidate(token);
        Object rolesClaim = c.get("roles", List.class);

        if (rolesClaim instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        } else if (rolesClaim instanceof String s) {
            return Arrays.stream(s.split("[ ,]")).map(String::trim).toList();
        }
        return List.of();
    }
}
