package com.eric.store.auth.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtKeyConfig {
    @Bean
    public SecretKey jwtHmacKey() {
        String b64 = System.getenv().getOrDefault("JWT_SECRET_B64",
                "u1xgO2mQwz0tQ0o1J5g8m7r2h9k2t5y8u1xgO2mQwz0tQ0o1J5g8m7r2h9k2t5y8");
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(b64));
    }
}