package com.eric.store.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {
    private static final String PREFIX = "2fa:";
    private final OtpService otpService;

    public void sendCode(String email) {
        otpService.sendCode(email, PREFIX);
    }

    public void verifyCode(String email, String code) {
        otpService.verifyCode(email, code, PREFIX);
    }
}

