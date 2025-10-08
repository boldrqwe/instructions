package com.example.instructions.api.dto;

import com.example.instructions.domain.ArticleStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO краткой информации о статье.
 */
@Data
public class ArticleSummaryDto {

    private UUID id;
    private String title;
    private String slug;
    private ArticleStatus status;
    private List<TagDto> tags = new ArrayList<>();
    private OffsetDateTime updatedAt;


}
