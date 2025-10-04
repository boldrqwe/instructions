package com.example.instructions.common;

/**
 * Исключение для операций без необходимых прав.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
