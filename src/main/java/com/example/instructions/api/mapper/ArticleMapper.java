package com.example.instructions.api.mapper;

import com.example.instructions.api.dto.*;
import com.example.instructions.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

/**
 * MapStruct-маппер для конвертации сущностей статьи в DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleMapper {

    @Mapping(target = "tags", source = "tagEntities")
    ArticleSummaryDto toSummary(Article article);

    List<ArticleSummaryDto> toSummaryList(List<Article> articles);

    List<TagDto> toTagDtos(Set<Tag> tags);

    List<ChapterDto> toChapterDtos(List<Chapter> chapters);

    @Mapping(target = "sections", source = "sections")
    @Mapping(target = "articleId", source = "article.id")
    ChapterDto toChapterDto(Chapter chapter);

    List<SectionDto> toSectionDtos(List<Section> sections);

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
    ArticleDto toPublicDto(Article article);
}
