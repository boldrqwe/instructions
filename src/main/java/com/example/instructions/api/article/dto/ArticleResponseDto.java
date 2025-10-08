package com.example.instructions.api.article.dto;

import com.example.instructions.domain.ArticleStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO ответа статьи.
 */
@Data
public class ArticleResponseDto {

    private UUID id;
    private String title;
    private String slug;
    private String summary;
    private String coverImageUrl;
    private List<String> tags;
    private String contentHtml;
    private JsonNode contentJson;
    private ArticleStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}