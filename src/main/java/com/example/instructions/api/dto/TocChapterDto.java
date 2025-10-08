package com.example.instructions.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO главы для оглавления.
 */
@Data
public class TocChapterDto {

    private UUID chapterId;
    private String chapterTitle;
    private List<TocSectionDto> sections = new ArrayList<>();

}
