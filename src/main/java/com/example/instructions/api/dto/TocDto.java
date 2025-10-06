package com.example.instructions.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO оглавления статьи.
 */
public class TocDto {

    private UUID articleId;
    private List<TocChapterDto> items = new ArrayList<>();

    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public List<TocChapterDto> getItems() {
        return items;
    }

    public void setItems(List<TocChapterDto> items) {
        this.items = items;
    }
}
