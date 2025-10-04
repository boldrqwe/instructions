package com.example.instructions.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO главы для оглавления.
 */
public class TocChapterDto {

    private UUID chapterId;
    private String chapterTitle;
    private List<TocSectionDto> sections = new ArrayList<>();

    public UUID getChapterId() {
        return chapterId;
    }

    public void setChapterId(UUID chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public List<TocSectionDto> getSections() {
        return sections;
    }

    public void setSections(List<TocSectionDto> sections) {
        this.sections = sections;
    }
}
