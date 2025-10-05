package com.example.instructions.common;

/**
 * Исключение для обозначения отсутствия запрошенного ресурса.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
