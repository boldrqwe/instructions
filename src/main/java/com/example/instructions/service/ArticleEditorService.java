package com.example.instructions.service;

import com.example.instructions.api.article.dto.ArticleCreateDto;
import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.api.article.dto.ArticleUpdateDto;
import com.example.instructions.api.article.mapper.ArticleEditorMapper;
import com.example.instructions.common.BadRequestException;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.common.PageResponse;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.repo.ArticleRepository;
import com.example.instructions.security.AuthenticationFacade;
import com.example.instructions.utils.SlugResolverService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Прикладной сервис для Article API редактора.
 */
@Service
@RequiredArgsConstructor
public class ArticleEditorService {

    private static final int MAX_CONTENT_HTML = 2_000_000;

    private final ArticleRepository articleRepository;
    private final ArticleEditorMapper mapper;
    private final AuthenticationFacade authenticationFacade;
    private final SlugResolverService slugResolverService;


    @Transactional
    public ArticleResponseDto create(ArticleCreateDto dto) {
        validateContentHtml(dto.getContentHtml());
        String slug = slugResolverService.resolveSlug(dto.getSlug(), dto.getTitle(), null);
        String currentUserId = authenticationFacade.getCurrentUserId();
        String contentHtml = defaultContentHtml(dto.getContentHtml());
        JsonNode contentJson = defaultContentJson(dto.getContentJson());
        String[] array = toArray(dto.getTags());
        Article article = mapper.toEntity(dto, slug, currentUserId, array, contentHtml, contentJson);
        Article saved = articleRepository.save(article);
        return mapper.toDto(saved);
    }

    @Transactional
    public ArticleResponseDto update(UUID id, ArticleUpdateDto dto) {
        validateContentHtml(dto.getContentHtml());
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        String slug = slugResolverService.resolveSlug(dto.getSlug(), article.getTitle(), article.getId());
        String contentHtml = defaultContentHtml(dto.getContentHtml());
        String[] array = toArray(dto.getTags());
        JsonNode contentJson = defaultContentJson(dto.getContentJson());
        Article toSave = mapper.updateEntity(article, dto, slug, array, contentHtml, contentJson);
        Article saved = articleRepository.save(toSave);
        return mapper.toDto(saved);
    }

    @Transactional
    public ArticleResponseDto publish(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setSlug(slugResolverService.resolveSlug(article.getSlug(), article.getTitle(), article.getId()));
        Article saved = articleRepository.save(article);
        return mapper.toDto(saved);
    }

    @Transactional
    public ArticleResponseDto unpublish(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        article.setStatus(ArticleStatus.DRAFT);
        Article saved = articleRepository.save(article);
        return mapper.toDto(saved);
    }

    public PageResponse<ArticleResponseDto> list(ArticleStatus status, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Article> result = articleRepository.search(status, query, pageable);
        List<ArticleResponseDto> content = result.map(mapper::toDto).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    public ArticleResponseDto get(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        return mapper.toDto(article);
    }

    public ArticleResponseDto getPublishedBySlug(String slug) {
        Article article = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        return mapper.toDto(article);
    }


    private String[] toArray(List<String> tags) {
        if (tags == null) {
            return null;
        }
        List<String> normalized = new ArrayList<>();
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String trimmed = tag.trim();
            normalized.add(trimmed);
        }
        return normalized.toArray(new String[0]);
    }

    private void validateContentHtml(String contentHtml) {
        if (contentHtml != null && contentHtml.length() > MAX_CONTENT_HTML) {
            throw new BadRequestException("HTML content is too large");
        }
    }

    private String defaultContentHtml(String contentHtml) {
        return contentHtml != null ? contentHtml : "";
    }

    private JsonNode defaultContentJson(JsonNode jsonNode) {
        return jsonNode != null ? jsonNode : JsonNodeFactory.instance.objectNode();
    }
}
