package com.example.instructions.article.mapper;

import com.example.instructions.article.dto.ArticleResponseDto;
import com.example.instructions.article.model.ArticleEntity;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Маппер статей.
 */
@Component
public class ArticleMapper {

    public ArticleResponseDto toResponseDto(ArticleEntity entity) {
        ArticleResponseDto dto = new ArticleResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSlug(entity.getSlug());
        dto.setStatus(entity.getStatus());
        dto.setSummary(entity.getSummary());
        dto.setContentHtml(entity.getContentHtml());
        dto.setContentJson(entity.getContentJson());
        dto.setCoverImageUrl(entity.getCoverImageUrl());
        dto.setTags(entity.getTags() == null ? List.of() : List.copyOf(entity.getTags()));
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public List<ArticleResponseDto> toResponseList(List<ArticleEntity> entities) {
        return entities.stream().filter(Objects::nonNull)
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}
