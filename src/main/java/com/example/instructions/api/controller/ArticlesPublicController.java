package com.example.instructions.api.controller;

import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.service.ArticleEditorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Публичные эндпоинты статей.
 */
@RestController
@RequestMapping("/api/v1/articles")
public class ArticlesPublicController {

    private final ArticleEditorService service;

    public ArticlesPublicController(ArticleEditorService service) {
        this.service = service;
    }

    @GetMapping("/by-slug/{slug}")
    public ArticleResponseDto getBySlug(@PathVariable String slug) {
        return service.getPublishedBySlug(slug);
    }
}
