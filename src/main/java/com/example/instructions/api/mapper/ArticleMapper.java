package com.example.instructions.api.mapper;

import com.example.instructions.api.dto.*;
import com.example.instructions.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct-маппер для конвертации сущностей статьи в DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleMapper {

    @Mapping(target = "tags", source = "tagEntities")
    ArticleSummaryDto toSummary(Article article);

    List<ArticleSummaryDto> toSummaryList(List<Article> articles);

    List<TagDto> toTagDtos(Set<Tag> tags);

    @Mapping(target = "sections", expression = "java(mapSections(chapter.getSections()))")
    @Mapping(target = "articleId", source = "article.id")
    ChapterDto toChapterDto(Chapter chapter);

    @Mapping(target = "chapterId", source = "chapter.id")
    SectionDto toSectionDto(Section section);

    TagDto toTagDto(Tag tag);


    /**
     * Создаёт новую сущность {@link Article} с заданными параметрами.
     * <p>
     * Поля:
     * <ul>
     *   <li>{@code slug} — присваивается из параметра {@code validateSlug}</li>
     *   <li>{@code status} — задаётся явно (например, {@link ArticleStatus#DRAFT})</li>
     *   <li>{@code createdBy} — устанавливается из {@code currentUserId}</li>
     *   <li>{@code tagEntities} — список тегов, связанных со статьёй</li>
     * </ul>
     *
     * @param validateSlug  slug статьи (уникальный идентификатор URL)
     * @param status        статус статьи ({@link ArticleStatus})
     * @param currentUserId идентификатор пользователя, создавшего статью
     * @param tagEntities   множество тегов, связанных со статьёй
     * @return новая сущность {@link Article}
     */
    @Mapping(target = "slug", source = "validateSlug")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdBy", source = "currentUserId")
    @Mapping(target = "tagEntities", source = "tagEntities")
    Article toEntity(String validateSlug,
                     ArticleStatus status,
                     String currentUserId,
                     Set<Tag> tagEntities);

    @Mapping(target = "description", source = "summary")
    @Mapping(target = "body", source = "contentHtml")
    @Mapping(target = "tags", source = "tagEntities")
    @Mapping(target = "chapters", expression = "java(mapChapters(article.getChapters()))")
    ArticleDto toPublicDto(Article article);

    default List<ChapterDto> mapChapters(Set<Chapter> chapters) {
        if (chapters == null || chapters.isEmpty()) {
            return Collections.emptyList();
        }
        return chapters.stream()
                .sorted(Comparator.comparingInt(Chapter::getOrderIndex))
                .map(this::toChapterDto)
                .collect(Collectors.toList());
    }

    default List<SectionDto> mapSections(Set<Section> sections) {
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        return sections.stream()
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .map(this::toSectionDto)
                .collect(Collectors.toList());
    }
}
