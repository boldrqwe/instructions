package com.example.instructions.api.controller;

import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.api.dto.ArticleDto;
import com.example.instructions.api.dto.TocDto;
import com.example.instructions.api.mapper.ArticleMapper;
import com.example.instructions.common.PageResponse;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.service.ArticleEditorService;
import com.example.instructions.service.ArticleService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticlesPublicController {

    private final ArticleService articleService;
    private final ArticleEditorService editorService;
    private final ArticleMapper mapper;

    @GetMapping("/by-slug/{slug}")
    public ArticleDto getBySlug(@PathVariable String slug) {
       return articleService.getPublishedArticle(slug);
    }

    @GetMapping("/{id}/toc")
    public TocDto getToc(@PathVariable UUID id) {
        return articleService.getPublishedToc(id);
    }


    @GetMapping
    public PageResponse<ArticleResponseDto> list(@RequestParam(required = false) ArticleStatus status,
                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Positive int size) {
        return editorService.findAll(status, page, size);
    }


}
