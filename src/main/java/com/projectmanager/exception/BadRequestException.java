package com.projectmanager.exception;

/**
 * Thrown when business validation rules are violated.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
