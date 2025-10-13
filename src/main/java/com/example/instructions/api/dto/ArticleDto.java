package com.example.instructions.api.dto;

import com.example.instructions.domain.ArticleStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO полной статьи.
 */
@Data
public class ArticleDto {

    private UUID id;
    private String title;
    private String slug;
    private ArticleStatus status;
    private String description; // <= из summary
    private String body;        // <= из contentHtml
    private OffsetDateTime updatedAt;
}
