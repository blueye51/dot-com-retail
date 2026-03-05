package com.eric.store.common.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, Exception ex) {
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        System.err.println(now + " EXCEPTION " + ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", now);
        body.put("error", error);
        body.put("message", ex.getMessage());
        body.put("status", status.value());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalJsonException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalJson(IllegalJsonException ex) {
        return build(HttpStatus.BAD_REQUEST, "Illegal JSON", ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not found", ex);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid refresh token", ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Entity not found", ex);
    }

    @ExceptionHandler(InvalidEmailOrPassword.class)
    public ResponseEntity<Map<String, Object>> handleInvalidEmailOrPassword(InvalidEmailOrPassword ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", ex);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Map<String, Object>> handleStorage(StorageException ex) {
        ex.printStackTrace(System.err);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", ex);
    }


    @ExceptionHandler(TurnstileException.class)
    public ResponseEntity<Map<String, Object>> handleTurnstile(TurnstileException ex) {
        return  build(HttpStatus.BAD_REQUEST, "Turnstile error", ex);
    }

    @ExceptionHandler(AuthProviderConflictException.class)
    public ResponseEntity<Map<String, Object>> handleAuthProviderConflict(AuthProviderConflictException ex) {
        return build(HttpStatus.CONFLICT, "Auth provider conflict", ex);
    }
}

