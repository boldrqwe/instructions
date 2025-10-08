package com.example.instructions.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO оглавления статьи.
 */
@Data
public class TocDto {

    private UUID articleId;
    private List<TocChapterDto> items = new ArrayList<>();

}
