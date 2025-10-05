package com.example.instructions.common;

/**
 * Исключение для неверных запросов клиента.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
