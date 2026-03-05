package com.eric.store.common.exceptions;

public class AuthProviderConflictException extends RuntimeException {
    public AuthProviderConflictException(String message) {
        super(message);
    }
}
