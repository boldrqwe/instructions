package com.example.instructions.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
        @NotBlank(message = "Заголовок обязателен")
        @Size(max = 200, message = "Заголовок не может быть длиннее 200 символов")
        String title,

        @NotBlank(message = "Содержимое обязательно")
        String content
) {
}
