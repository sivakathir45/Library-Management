package com.psgitech.library.exception;

/**
 * Thrown when a requested book title cannot be located in the catalog.
 */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
