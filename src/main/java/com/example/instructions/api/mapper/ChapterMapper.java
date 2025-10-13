package com.example.instructions.api.mapper;


import com.example.instructions.api.model.ChapterCreateRequest;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct-маппер для создания и преобразования глав (Chapter).
 * <p>
 * Используется для формирования новой сущности {@link Chapter}
 * при добавлении главы в статью.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterMapper {

    /**
     * Создаёт новую сущность {@link Chapter} на основе данных из {@link ChapterCreateRequest}.
     * <p>
     * Устанавливает:
     * <ul>
     *   <li>{@link Chapter#setArticle(Article)} — привязка к статье</li>
     *   <li>{@link Chapter#setOrderIndex(Integer)} — порядок главы в статье</li>
     *   <li>{@link Chapter#setTitle(String)} — заголовок главы</li>
     * </ul>
     *
     * @param request DTO с данными новой главы
     * @param article сущность {@link Article}, к которой принадлежит глава
     * @return новая сущность {@link Chapter}
     */
    @Mapping(target = "article", source = "article")
    @Mapping(target = "orderIndex", source = "request.orderIndex")
    @Mapping(target = "title", source = "request.title")
    Chapter toEntity(ChapterCreateRequest request, Article article);
}
