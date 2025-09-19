package com.user.service.userservice.exception;

import lombok.Builder;

/**
 * Exception thrown when user provides invalid credentials during login
 */
public class InvalidCredentialsException extends RuntimeException {

    @Builder
    public InvalidCredentialsException(String message) {
        super(message);
    }

    @Builder
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
