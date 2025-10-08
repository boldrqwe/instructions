package com.example.instructions.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.*;

import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Сущность статьи с оглавлением и тегами.
 */
@Entity
@Table(name = "article")
@Data
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "slug", nullable = false, length = 512)
    private String slug;

    @Column(name = "summary")
    private String summary;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "content_html", columnDefinition = "TEXT", nullable = false)
    private String contentHtml = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", columnDefinition = "jsonb", nullable = false)
    private JsonNode contentJson = JsonNodeFactory.instance.objectNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @jakarta.persistence.OrderBy("orderIndex ASC")
    private Set<Chapter> chapters = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tagEntities = new LinkedHashSet<>();


    /**
     * Добавляет главу в статью, автоматически устанавливая обратную ссылку.
     *
     * @param chapter глава
     */
    public void addChapter(Chapter chapter) {
        chapter.setArticle(this);
        this.chapters.add(chapter);
    }

    /**
     * Удаляет главу из статьи, поддерживая согласованность коллекции.
     *
     * @param chapter удаляемая глава
     */
    public void removeChapter(Chapter chapter) {
        chapter.setArticle(null);
        this.chapters.remove(chapter);
    }

    @PrePersist
    void onPersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.contentHtml == null) {
            this.contentHtml = "";
        }
        if (this.contentJson == null) {
            this.contentJson = JsonNodeFactory.instance.objectNode();
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        if (this.contentHtml == null) {
            this.contentHtml = "";
        }
        if (this.contentJson == null) {
            this.contentJson = JsonNodeFactory.instance.objectNode();
        }
    }
}
