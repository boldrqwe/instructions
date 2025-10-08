package com.example.instructions.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос на создание черновика статьи.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDraftCreateRequest {

    @NotBlank
    @Size(max = 512)
    private String title;

    @Size(max = 512)
    private String slug;

    private List<@Size(max = 128) String> tags = new ArrayList<>();

}
