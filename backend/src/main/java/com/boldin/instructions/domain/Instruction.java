package com.boldin.instructions.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instructions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "slug")
})
public class Instruction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private InstructionCategory category;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 512)
    private String summary;

    @Lob
    @Column(name = "introduction", nullable = false)
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InstructionDifficulty difficulty;

    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes;

    @Column(length = 1024)
    private String prerequisites;

    @ElementCollection
    @CollectionTable(name = "instruction_tags", joinColumns = @JoinColumn(name = "instruction_id"))
    @Column(name = "tag", length = 64, nullable = false)
    @OrderColumn(name = "tag_order")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "instruction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<InstructionSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "instruction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<InstructionResource> resources = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Instruction() {
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public InstructionCategory getCategory() {
        return category;
    }

    public void setCategory(InstructionCategory category) {
        this.category = category;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public InstructionDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(InstructionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    public List<InstructionSection> getSections() {
        return sections;
    }

    public void setSections(List<InstructionSection> sections) {
        this.sections.clear();
        if (sections != null) {
            for (InstructionSection section : sections) {
                section.setInstruction(this);
                this.sections.add(section);
            }
        }
    }

    public List<InstructionResource> getResources() {
        return resources;
    }

    public void setResources(List<InstructionResource> resources) {
        this.resources.clear();
        if (resources != null) {
            for (InstructionResource resource : resources) {
                resource.setInstruction(this);
                this.resources.add(resource);
            }
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
