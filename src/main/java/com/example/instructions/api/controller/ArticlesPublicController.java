package com.example.instructions.api.controller;

import com.example.instructions.api.dto.ArticleDto;
import com.example.instructions.api.dto.TocDto;
import com.example.instructions.api.mapper.ArticleMapper;
import com.example.instructions.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticlesPublicController {

    private final ArticleService articleService;
    private final ArticleMapper mapper;

    @GetMapping("/by-slug/{slug}")
    public ArticleDto getBySlug(@PathVariable String slug) {
       return articleService.getPublishedArticle(slug);
    }

    @GetMapping("/{id}/toc")
    public TocDto getToc(@PathVariable UUID id) {
        return articleService.getPublishedToc(id);
    }
}
