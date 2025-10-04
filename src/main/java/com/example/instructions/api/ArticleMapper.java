package com.example.instructions.api;

import com.example.instructions.domain.Article;
import com.example.instructions.domain.Chapter;
import com.example.instructions.domain.Section;
import com.example.instructions.domain.Tag;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct-маппер для конвертации сущностей статьи в DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleMapper {

    @Mapping(target = "chapters", source = "chapters")
    @Mapping(target = "tags", source = "tags")
    ArticleDto toDto(Article article);

    @Mapping(target = "tags", source = "tags")
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
}
