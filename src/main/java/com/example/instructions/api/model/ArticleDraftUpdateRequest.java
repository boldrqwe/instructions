package com.example.instructions.api.model;

import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Запрос на обновление черновика статьи.
 */
public class ArticleDraftUpdateRequest {

    @Size(max = 512)
    private String title;

    @Size(max = 512)
    private String slug;

    private List<@Size(max = 128) String> tags = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
