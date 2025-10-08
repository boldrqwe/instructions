package com.example.instructions.api.article.dto;

import com.example.instructions.domain.ArticleStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO обновления статьи.
 */
@Data
public class ArticleUpdateDto {

    @Size(min = 2, max = 200)
    private String title;

    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must match ^[a-z0-9-]+$")
    private String slug;

    private String summary;

    @Size(max = 20)
    private List<@Size(min = 1, max = 64) String> tags;

    private String coverImageUrl;

    @Size(max = 2_000_000)
    private String contentHtml;

    private JsonNode contentJson;

    private ArticleStatus status;

}
