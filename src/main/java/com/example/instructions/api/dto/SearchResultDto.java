package com.example.instructions.api.dto;

import com.example.instructions.api.enums.SearchResultType;
import lombok.Data;

import java.util.UUID;

/**
 * DTO результата поиска.
 */
@Data
public class SearchResultDto {

    private SearchResultType type;
    private UUID id;
    private String title;
    private String snippet;
    private String slug;
}
