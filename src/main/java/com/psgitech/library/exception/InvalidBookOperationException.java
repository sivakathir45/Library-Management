package com.psgitech.library.exception;

/**
 * Thrown when a book or physical copy operation violates library business rules or archival guardrails.
 */
public class InvalidBookOperationException extends RuntimeException {
    public InvalidBookOperationException(String message) {
        super(message);
    }
}
