package com.example.instructions.article.controller;

import com.example.instructions.article.dto.ArticleCreateDto;
import com.example.instructions.article.dto.ArticleResponseDto;
import com.example.instructions.article.dto.ArticleUpdateDto;
import com.example.instructions.article.model.ArticleStatus;
import com.example.instructions.article.service.ArticleService;
import com.example.instructions.common.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для управления статьями и публичного чтения.
 */
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ArticleResponseDto createArticle(@Valid @RequestBody ArticleCreateDto dto) {
        return articleService.createArticle(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ArticleResponseDto updateArticle(@PathVariable UUID id, @Valid @RequestBody ArticleUpdateDto dto) {
        return articleService.updateArticle(id, dto);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ArticleResponseDto publishArticle(@PathVariable UUID id) {
        return articleService.publishArticle(id);
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ArticleResponseDto unpublishArticle(@PathVariable UUID id) {
        return articleService.unpublishArticle(id);
    }

    @GetMapping
    public PageResponse<ArticleResponseDto> listArticles(@RequestParam(required = false) ArticleStatus status,
                                                         @RequestParam(required = false) String query,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @RequestParam(required = false) String authorId,
                                                         Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        if (!isAdmin) {
            status = ArticleStatus.PUBLISHED;
            authorId = null;
        }
        return articleService.searchArticles(status, query, page, size, authorId, isAdmin);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ArticleResponseDto getArticle(@PathVariable UUID id) {
        return articleService.getArticle(id);
    }

    @GetMapping("/by-slug/{slug}")
    public ArticleResponseDto getBySlug(@PathVariable String slug) {
        return articleService.getPublishedBySlug(slug);
    }

    private boolean hasAdminRole(Authentication authentication) {
        Authentication auth = authentication;
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (StringUtils.hasText(authority.getAuthority()) && authority.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }
}
