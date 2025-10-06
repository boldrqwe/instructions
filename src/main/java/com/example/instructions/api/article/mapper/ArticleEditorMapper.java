package com.example.instructions.api.article.mapper;

import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.domain.Article;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct-маппер для редактора статей.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleEditorMapper {

    @Mapping(target = "tags", expression = "java(toList(article.getTags()))")
    ArticleResponseDto toDto(Article article);

    default List<String> toList(String[] tags) {
        if (tags == null || tags.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(tags);
    }
}
