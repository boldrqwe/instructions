package com.example.instructions.common;

import com.example.instructions.article.exception.ArticleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.toList());

        ApiError apiError = ApiError.of(status.value(), status.getReasonPhrase(), "Данные заполнены некорректно", details);
        return new ResponseEntity<>(apiError, headers, status);
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ApiError> handleArticleNotFound(ArticleNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Неверные параметры запроса", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknownError(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError apiError = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                "Неожиданная ошибка. Попробуйте повторить запрос позже.",
                List.of("Путь: " + request.getRequestURI())
        );
        return ResponseEntity.status(status).body(apiError);
    }

    private static String formatFieldError(FieldError error) {
        return "%s: %s".formatted(error.getField(), error.getDefaultMessage());
    }
}
