package com.example.instructions.service;

import com.example.instructions.api.article.dto.ArticleCreateDto;
import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.api.article.dto.ArticleUpdateDto;
import com.example.instructions.api.article.mapper.ArticleEditorMapper;
import com.example.instructions.common.BadRequestException;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.common.PageResponse;
import com.example.instructions.common.SlugGenerator;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.repo.ArticleRepository;
import com.example.instructions.security.AuthenticationFacade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Прикладной сервис для Article API редактора.
 */
@Service
public class ArticleEditorService {

    private static final int MAX_CONTENT_HTML = 2_000_000;

    private final ArticleRepository articleRepository;
    private final ArticleEditorMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public ArticleEditorService(ArticleRepository articleRepository,
                                ArticleEditorMapper mapper,
                                AuthenticationFacade authenticationFacade) {
        this.articleRepository = articleRepository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Transactional
    public ArticleResponseDto create(ArticleCreateDto dto) {
        validateContentHtml(dto.getContentHtml());
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSlug(resolveSlug(dto.getSlug(), dto.getTitle(), null));
        article.setSummary(dto.getSummary());
        article.setCoverImageUrl(dto.getCoverImageUrl());
        article.setTags(toArray(dto.getTags()));
        article.setContentHtml(defaultContentHtml(dto.getContentHtml()));
        article.setContentJson(defaultContentJson(dto.getContentJson()));
        article.setStatus(ArticleStatus.DRAFT);
        article.setCreatedBy(authenticationFacade.getCurrentUserId());
        Article saved = articleRepository.save(article);
        return mapper.toDto(saved);
    }

    @Transactional
    public ArticleResponseDto update(UUID id, ArticleUpdateDto dto) {
        validateContentHtml(dto.getContentHtml());
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));

        if (StringUtils.hasText(dto.getTitle())) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getSlug() != null) {
            article.setSlug(resolveSlug(dto.getSlug(), article.getTitle(), article.getId()));
        }
        if (dto.getSummary() != null) {
            article.setSummary(dto.getSummary());
        }
        if (dto.getCoverImageUrl() != null) {
            article.setCoverImageUrl(dto.getCoverImageUrl());
        }
        if (dto.getTags() != null) {
            article.setTags(toArray(dto.getTags()));
        }
        if (dto.getContentHtml() != null) {
            article.setContentHtml(defaultContentHtml(dto.getContentHtml()));
        }
        if (dto.getContentJson() != null) {
            article.setContentJson(defaultContentJson(dto.getContentJson()));
        }
        if (dto.getStatus() != null) {
            article.setStatus(dto.getStatus());
        }
        Article saved = articleRepository.save(article);
        return mapper.toDto(saved);
    }

    @Transactional
    public ArticleResponseDto publish(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setSlug(resolveSlug(article.getSlug(), article.getTitle(), article.getId()));
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

    private String resolveSlug(String providedSlug, String title, UUID currentId) {
        String baseSlug;
        if (StringUtils.hasText(providedSlug)) {
            baseSlug = providedSlug.trim().toLowerCase(Locale.ROOT);
        } else if (StringUtils.hasText(title)) {
            baseSlug = SlugGenerator.fromText(title);
        } else {
            throw new BadRequestException("Slug or title must be provided");
        }
        if (!baseSlug.matches("^[a-z0-9-]+$")) {
            throw new BadRequestException("Недопустимый slug");
        }
        String candidate = baseSlug;
        int counter = 2;
        while (true) {
            Optional<Article> existing = articleRepository.findBySlug(candidate);
            if (existing.isEmpty() || (currentId != null && existing.get().getId().equals(currentId))) {
                return candidate;
            }
            candidate = baseSlug + "-" + counter++;
        }
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
