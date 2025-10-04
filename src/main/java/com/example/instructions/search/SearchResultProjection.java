package com.example.instructions.search;

import java.util.UUID;

/**
 * Проекция результата поиска.
 */
public class SearchResultProjection {

    private final String type;
    private final UUID id;
    private final String title;
    private final String markdown;
    private final String slug;
    private final double rank;

    public SearchResultProjection(String type, UUID id, String title, String markdown, String slug, double rank) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.markdown = markdown;
        this.slug = slug;
        this.rank = rank;
    }

    public String getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMarkdown() {
        return markdown;
    }

    public String getSlug() {
        return slug;
    }

    public double getRank() {
        return rank;
    }
}
