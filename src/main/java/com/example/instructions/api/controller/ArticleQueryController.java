package com.example.instructions.api.controller;

import com.example.instructions.api.dto.ArticleDto;
import com.example.instructions.api.dto.ArticleSummaryDto;
import com.example.instructions.api.dto.TocDto;
import com.example.instructions.common.PageResponse;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.service.ArticleService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Публичные REST-эндпоинты для чтения статей.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class ArticleQueryController {

    private final ArticleService articleService;

    public ArticleQueryController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/articles")
    public PageResponse<ArticleSummaryDto> getArticles(
            @RequestParam(name = "status", required = false) ArticleStatus status,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Positive int size) {
        return articleService.getArticles(status, query, page, size);
    }

    @GetMapping("/articles/{slug}")
    public ArticleDto getArticle(@PathVariable String slug) {
        return articleService.getPublishedArticle(slug);
    }

    @GetMapping("/articles/{id}/toc")
    public TocDto getToc(@PathVariable UUID id) {
        return articleService.getPublishedToc(id);
    }
}
