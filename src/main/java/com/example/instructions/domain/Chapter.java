package com.example.instructions.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.*;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SecondaryRow;
import org.hibernate.type.SqlTypes;

/**
 * Глава статьи.
 */
@Entity
@Table(name = "chapter")
@Getter
@Setter
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private Set<Section> sections = new HashSet<>();

    /**
     * Добавляет секцию в главу с установкой обратной ссылки.
     *
     * @param section секция
     */
    public void addSection(Section section) {
        section.setChapter(this);
        this.sections.add(section);
    }

    /**
     * Удаляет секцию и очищает обратную ссылку.
     *
     * @param section удаляемая секция
     */
    public void removeSection(Section section) {
        section.setChapter(null);
        this.sections.remove(section);
    }
}
