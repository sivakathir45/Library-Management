package com.psgitech.library.exception;

/**
 * Thrown when attempting to register a book with an ISBN that already exists in the catalog.
 */
public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String message) {
        super(message);
    }
}
