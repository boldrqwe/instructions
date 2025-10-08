package com.example.instructions.api.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO тега статьи.
 */
@Data
public class TagDto {

    private UUID id;
    private String name;
    private String slug;


}
