package com.example.instructions.article.exception;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super("Статья с идентификатором %d не найдена".formatted(id));
    }
}
