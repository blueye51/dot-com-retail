package com.eric.store.common.exceptions;

public class InvalidEmailOrPassword extends RuntimeException {
    public InvalidEmailOrPassword(String message) {
        super(message);
    }
}
