package com.example.instructions.api.mapper;

import com.example.instructions.api.dto.TocChapterDto;
import com.example.instructions.api.dto.TocDto;
import com.example.instructions.api.dto.TocSectionDto;
import com.example.instructions.domain.Chapter;
import com.example.instructions.domain.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

/**
 * Маппер для построения оглавления.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TocMapper {

    @Mapping(target = "items", source = "chapters")
    @Mapping(target = "articleId", source = "articleId")
    TocDto toTocDto(java.util.UUID articleId, Set<Chapter> chapters);

    @Mapping(target = "chapterId", source = "id")
    @Mapping(target = "chapterTitle", source = "title")
    @Mapping(target = "sections", source = "sections")
    TocChapterDto toChapterDto(Chapter chapter);

    List<TocChapterDto> toChapterDtos(List<Chapter> chapters);

    @Mapping(target = "sectionId", source = "id")
    @Mapping(target = "sectionTitle", source = "title")
    TocSectionDto toSectionDto(Section section);

    List<TocSectionDto> toSectionDtos(List<Section> sections);
}
