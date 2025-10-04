package com.example.instructions.api;

import java.util.UUID;

/**
 * DTO тега статьи.
 */
public class TagDto {

    private UUID id;
    private String name;
    private String slug;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
