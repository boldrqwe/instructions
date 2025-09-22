package com.example.instructions.article.dto;

import java.time.OffsetDateTime;

public record ArticleResponse(
        Long id,
        String title,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
