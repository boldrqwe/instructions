package com.example.instructions.common;

/**
 * DTO для описания ошибки API.
 */
public class ApiErrorResponse {

    private final ErrorCode code;
    private final String message;

    public ApiErrorResponse(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
