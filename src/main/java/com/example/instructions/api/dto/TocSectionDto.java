package com.example.instructions.api.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO секции в оглавлении.
 */
@Data
public class TocSectionDto {

    private UUID sectionId;
    private String sectionTitle;


}
