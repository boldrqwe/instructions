package com.example.instructions.api;

import com.example.instructions.service.ArticleService;
import com.example.instructions.service.PublicationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Административные эндпоинты управления статьями.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class ArticleAdminController {

    private final ArticleService articleService;
    private final PublicationService publicationService;

    public ArticleAdminController(ArticleService articleService, PublicationService publicationService) {
        this.articleService = articleService;
        this.publicationService = publicationService;
    }

    @PostMapping("/articles")
    public ArticleDto createDraft(@Valid @RequestBody ArticleDraftCreateRequest request) {
        return articleService.createDraft(request);
    }

    @PutMapping("/articles/{id}")
    public ArticleDto updateDraft(@PathVariable UUID id, @Valid @RequestBody ArticleDraftUpdateRequest request) {
        return articleService.updateDraft(id, request);
    }

    @PostMapping("/articles/{id}:publish")
    public ArticleDto publish(@PathVariable UUID id) {
        return publicationService.publishArticle(id);
    }

    @PostMapping("/chapters")
    public ChapterDto createChapter(@Valid @RequestBody ChapterCreateRequest request) {
        return articleService.createChapter(request);
    }

    @PostMapping("/sections")
    public SectionDto createSection(@Valid @RequestBody SectionCreateRequest request) {
        return articleService.createSection(request);
    }
}
