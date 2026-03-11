package com.eric.store.auth.dto;

public record Verify2faRequest(
        String tempCode,
        String otpCode
) {
}
