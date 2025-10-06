package com.example.instructions.common;

/**
 * Исключение превышения лимита запросов.
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
