package com.example.instructions.api.dto;

import com.example.instructions.domain.ArticleStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private int version;
    private List<ChapterDto> chapters = new ArrayList<>();
    private List<TagDto> tags = new ArrayList<>();
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
