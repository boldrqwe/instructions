package com.example.instructions.api;

import java.util.UUID;

/**
 * DTO секции в оглавлении.
 */
public class TocSectionDto {

    private UUID sectionId;
    private String sectionTitle;

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }
}
