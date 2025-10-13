package com.example.instructions.api.mapper;

import com.example.instructions.api.model.SectionCreateRequest;
import com.example.instructions.domain.Chapter;
import com.example.instructions.domain.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct-маппер для создания и преобразования секций (Section).
 * <p>
 * Используется для формирования новой сущности {@link Section}
 * при добавлении секции в главу.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SectionMapper {

    /**
     * Создаёт новую сущность {@link Section} на основе данных из {@link SectionCreateRequest}.
     * <p>
     * Устанавливает:
     * <ul>
     *   <li>{@link Section#setChapter(Chapter)} — привязку к главе</li>
     *   <li>{@link Section#setOrderIndex(Integer)} — порядок секции в главе</li>
     *   <li>{@link Section#setTitle(String)} — заголовок секции</li>
     *   <li>{@link Section#setMarkdown(String)} — содержимое секции в формате Markdown</li>
     * </ul>
     *
     * @param request DTO с данными новой секции
     * @param chapter сущность {@link Chapter}, к которой принадлежит секция
     * @return новая сущность {@link Section}
     */
    @Mapping(target = "chapter", source = "chapter")
    @Mapping(target = "orderIndex", source = "request.orderIndex")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "markdown", source = "request.markdown")
    Section toEntity(SectionCreateRequest request, Chapter chapter);
}

