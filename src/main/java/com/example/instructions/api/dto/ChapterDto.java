package com.example.instructions.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO главы.
 */
public class ChapterDto {

    private UUID id;
    private UUID articleId;
    private String title;
    private int orderIndex;
    private List<SectionDto> sections = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<SectionDto> getSections() {
        return sections;
    }

    public void setSections(List<SectionDto> sections) {
        this.sections = sections;
    }
}
