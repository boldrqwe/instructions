package com.example.instructions.api.article.mapper;

import com.example.instructions.api.article.dto.ArticleCreateDto;
import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.api.article.dto.ArticleUpdateDto;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * MapStruct-маппер для преобразования сущностей и DTO статей.
 * <p>
 * Используется редактором для создания, обновления и отображения статей.
 * Поддерживает частичные обновления (patch), корректное преобразование тегов и контента.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleEditorMapper {

    /**
     * Преобразует {@link ArticleCreateDto} в сущность {@link Article}.
     * <p>
     * Устанавливает:
     * <ul>
     *   <li>Статус — {@link ArticleStatus#DRAFT}</li>
     *   <li>Создателя статьи — {@code currentUserId}</li>
     *   <li>Slug, HTML и JSON контент, теги — из параметров</li>
     * </ul>
     *
     * @param dto            DTO с данными для создания статьи
     * @param slug           уникальный slug статьи
     * @param currentUserId  идентификатор пользователя, создающего статью
     * @param array          массив тегов
     * @param contentHtml    HTML-контент статьи
     * @param contentJson    JSON-представление контента
     * @return новая сущность {@link Article}
     */
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "tags", expression = "java(array)")
    @Mapping(target = "contentHtml", source = "contentHtml")
    @Mapping(target = "contentJson", source = "contentJson")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "createdBy", source = "currentUserId")
    Article toEntity(ArticleCreateDto dto,
                     String slug,
                     String currentUserId,
                     String[] array,
                     String contentHtml,
                     JsonNode contentJson);

    /**
     * Обновляет существующую сущность {@link Article} на основе данных из {@link ArticleUpdateDto}.
     * <p>
     * Поля с {@code null} в DTO игнорируются, сохраняются прежние значения.
     * Переданные параметры (slug, contentHtml, contentJson, array) имеют приоритет над DTO.
     *
     * @param article       обновляемая сущность
     * @param dto           DTO с новыми данными
     * @param slug          новый slug (или {@code null} — оставить старый)
     * @param array         новый массив тегов (или {@code null} — оставить старые)
     * @param contentHtml   новый HTML-контент (или {@code null} — оставить старый)
     * @param contentJson   новый JSON-контент (или {@code null} — оставить старый)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "slug", expression = "java(slug != null ? slug : article.getSlug())")
    @Mapping(target = "tags", expression = "java(array != null ? array : article.getTags())")
    @Mapping(target = "contentHtml", expression = "java(contentHtml != null ? contentHtml : article.getContentHtml())")
    @Mapping(target = "contentJson", expression = "java(contentJson != null ? contentJson : article.getContentJson())")
    Article updateEntity(@MappingTarget Article article,
                      ArticleUpdateDto dto,
                      String slug,
                      String[] array,
                      String contentHtml,
                      JsonNode contentJson);

    /**
     * Преобразует сущность {@link Article} в DTO {@link ArticleResponseDto}.
     * <p>
     * Конвертирует массив тегов в {@link List}.
     *
     * @param article сущность статьи
     * @return DTO для отображения статьи
     */
    @Mapping(target = "tags", expression = "java(toList(article.getTags()))")
    ArticleResponseDto toDto(Article article);

    /**
     * Преобразует массив тегов в список строк.
     *
     * @param tags массив тегов
     * @return список тегов (пустой, если null или пуст)
     */
    default List<String> toList(String[] tags) {
        if (tags == null || tags.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(tags);
    }
}
