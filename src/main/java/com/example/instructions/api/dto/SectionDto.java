package com.example.instructions.api.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO секции статьи.
 */
@Data
public class SectionDto {

    private UUID id;
    private UUID chapterId;
    private String title;
    private int orderIndex;
    private String markdown;


}
