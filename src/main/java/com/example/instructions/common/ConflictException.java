package com.example.instructions.common;

/**
 * Исключение для конфликтных операций.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
