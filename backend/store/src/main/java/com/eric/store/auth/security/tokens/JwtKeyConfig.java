package com.eric.store.auth.security.tokens;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtKeyConfig {
    @Bean
    public SecretKey jwtHmacKey(@Value("${app.jwt.secret-b64}") String b64) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(b64));
    }
}