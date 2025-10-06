package com.example.instructions.api.controller;

import com.example.instructions.api.article.dto.ArticleCreateDto;
import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.api.article.dto.ArticleUpdateDto;
import com.example.instructions.common.PageResponse;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.service.ArticleEditorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Административный контроллер Article API.
 */
@RestController
@RequestMapping("/api/v1/articles")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class ArticlesAdminController {

    private final ArticleEditorService service;

    public ArticlesAdminController(ArticleEditorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponseDto create(@Valid @RequestBody ArticleCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ArticleResponseDto update(@PathVariable UUID id, @Valid @RequestBody ArticleUpdateDto dto) {
        return service.update(id, dto);
    }

    @PostMapping("/{id}/publish")
    public ArticleResponseDto publish(@PathVariable UUID id) {
        return service.publish(id);
    }

    @PostMapping("/{id}/unpublish")
    public ArticleResponseDto unpublish(@PathVariable UUID id) {
        return service.unpublish(id);
    }

    @GetMapping
    public PageResponse<ArticleResponseDto> list(@RequestParam(required = false) ArticleStatus status,
                                                 @RequestParam(required = false) String query,
                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Positive int size) {
        return service.list(status, query, page, size);
    }

    @GetMapping("/{id}")
    public ArticleResponseDto get(@PathVariable UUID id) {
        return service.get(id);
    }
}
