package com.example.instructions.article.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO создания статьи.
 */
public class ArticleCreateDto {

    @NotBlank
    @Size(min = 2, max = 200)
    private String title;

    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug может содержать только строчные латинские буквы, цифры и тире")
    @Size(min = 2, max = 200)
    private String slug;

    @Size(max = 4000)
    private String summary;

    @Size(max = 2_000_000)
    private String contentHtml;

    @NotNull
    private JsonNode contentJson;

    @Size(max = 1024)
    private String coverImageUrl;

    @Size(max = 20)
    private List<@Size(min = 1, max = 128) String> tags = new ArrayList<>();

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public JsonNode getContentJson() {
        return contentJson;
    }

    public void setContentJson(JsonNode contentJson) {
        this.contentJson = contentJson;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
