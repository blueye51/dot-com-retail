package com.eric.store.auth.security.turnstile;

import com.eric.store.common.exceptions.TurnstileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TurnstileService {

    private final RestClient restClient;
    private final String secret;

    public TurnstileService(RestClient.Builder builder,
                            @Value("${app.turnstile.secret}") String secret) {
        this.restClient = builder.baseUrl("https://challenges.cloudflare.com").build();
        this.secret = secret;
    }

    public void verifyOrThrow(String token) {
        if (token == null || token.isBlank()) {
            throw new TurnstileException("Missing Turnstile token");
        }

        var body = "secret=" + enc(secret) + "&response=" + enc(token);

        TurnstileResponse res = restClient.post()
                .uri("/turnstile/v0/siteverify")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TurnstileResponse.class);

        if (res == null || res.success == null || !res.success) {
            throw new TurnstileException("Turnstile verification failed: " +
                    (res == null ? "no-response" : String.valueOf(res.errorCodes)));
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // Jackson will map snake_case from Cloudflare into these if you use matching names or @JsonProperty.
    public static class TurnstileResponse {
        public Boolean success;
        public String challenge_ts;
        public String hostname;
        public List<String> errorCodes;
    }
}