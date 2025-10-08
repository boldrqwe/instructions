package com.example.instructions.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO главы.
 */
@Data
public class ChapterDto {

    private UUID id;
    private UUID articleId;
    private String title;
    private int orderIndex;
    private List<SectionDto> sections = new ArrayList<>();


}
