package com.example.instructions.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Запрос на создание главы.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterCreateRequest {

    @NotNull
    private UUID articleId;

    @NotBlank
    @Size(max = 512)
    private String title;

    @Min(0)
    private int orderIndex;

}
