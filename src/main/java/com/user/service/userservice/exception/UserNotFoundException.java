package com.user.service.userservice.exception;

import lombok.Builder;

/**
 * Exception thrown when user is not found during login
 */
public class UserNotFoundException extends RuntimeException {

    @Builder
    public UserNotFoundException(String message) {
        super(message);
    }

    @Builder
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
