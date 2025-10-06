package com.example.instructions.article.service;

import com.example.instructions.article.dto.ArticleCreateDto;
import com.example.instructions.article.dto.ArticleResponseDto;
import com.example.instructions.article.dto.ArticleUpdateDto;
import com.example.instructions.article.mapper.ArticleMapper;
import com.example.instructions.article.model.ArticleEntity;
import com.example.instructions.article.model.ArticleStatus;
import com.example.instructions.article.repository.ArticleRepository;
import com.example.instructions.common.BadRequestException;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.common.PageResponse;
import com.example.instructions.common.SlugGenerator;
import com.example.instructions.security.AuthenticationFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Сервис управления статьями.
 */
@Service
public class ArticleService {

    private static final int MAX_SLUG_LENGTH = 200;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final AuthenticationFacade authenticationFacade;

    public ArticleService(ArticleRepository articleRepository,
                          ArticleMapper articleMapper,
                          AuthenticationFacade authenticationFacade) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Transactional
    public ArticleResponseDto createArticle(ArticleCreateDto dto) {
        ArticleEntity entity = new ArticleEntity();
        entity.setTitle(dto.getTitle());
        entity.setSummary(dto.getSummary());
        entity.setContentHtml(dto.getContentHtml() != null ? dto.getContentHtml() : "");
        entity.setContentJson(Optional.ofNullable(dto.getContentJson()).orElseGet(OBJECT_MAPPER::createObjectNode));
        entity.setCoverImageUrl(dto.getCoverImageUrl());
        entity.setTags(normalizeTags(dto.getTags()));
        entity.setCreatedBy(authenticationFacade.getCurrentUserId());
        String slugCandidate = resolveSlugCandidate(dto.getSlug(), dto.getTitle());
        entity.setSlug(generateUniqueSlug(slugCandidate, null));
        entity.setStatus(ArticleStatus.DRAFT);
        ArticleEntity saved = articleRepository.save(entity);
        return articleMapper.toResponseDto(saved);
    }

    @Transactional
    public ArticleResponseDto updateArticle(UUID id, ArticleUpdateDto dto) {
        ArticleEntity entity = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        if (StringUtils.hasText(dto.getTitle())) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getSummary() != null) {
            entity.setSummary(dto.getSummary());
        }
        if (dto.getContentHtml() != null) {
            entity.setContentHtml(dto.getContentHtml());
        }
        if (dto.getContentJson() != null) {
            entity.setContentJson(dto.getContentJson());
        }
        if (dto.getCoverImageUrl() != null) {
            entity.setCoverImageUrl(dto.getCoverImageUrl());
        }
        if (dto.getTags() != null) {
            entity.setTags(normalizeTags(dto.getTags()));
        }
        if (dto.getSlug() != null) {
            String slugCandidate = resolveSlugCandidate(dto.getSlug(), Optional.ofNullable(dto.getTitle()).orElse(entity.getTitle()));
            entity.setSlug(generateUniqueSlug(slugCandidate, entity.getId()));
        } else if (dto.getTitle() != null && !StringUtils.hasText(entity.getSlug())) {
            entity.setSlug(generateUniqueSlug(SlugGenerator.fromText(dto.getTitle()), entity.getId()));
        }
        ArticleEntity saved = articleRepository.save(entity);
        return articleMapper.toResponseDto(saved);
    }

    @Transactional
    public ArticleResponseDto publishArticle(UUID id) {
        ArticleEntity entity = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        entity.setStatus(ArticleStatus.PUBLISHED);
        ArticleEntity saved = articleRepository.save(entity);
        return articleMapper.toResponseDto(saved);
    }

    @Transactional
    public ArticleResponseDto unpublishArticle(UUID id) {
        ArticleEntity entity = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        entity.setStatus(ArticleStatus.DRAFT);
        ArticleEntity saved = articleRepository.save(entity);
        return articleMapper.toResponseDto(saved);
    }

    public ArticleResponseDto getArticle(UUID id) {
        ArticleEntity entity = articleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        return articleMapper.toResponseDto(entity);
    }

    public ArticleResponseDto getPublishedBySlug(String slug) {
        ArticleEntity entity = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        return articleMapper.toResponseDto(entity);
    }

    public PageResponse<ArticleResponseDto> searchArticles(ArticleStatus status, String query, int page, int size, String authorId, boolean isAdmin) {
        ArticleStatus effectiveStatus = isAdmin ? status : ArticleStatus.PUBLISHED;
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleEntity> result = articleRepository.search(effectiveStatus != null ? effectiveStatus.name() : null,
                StringUtils.hasText(query) ? query : null,
                StringUtils.hasText(authorId) ? authorId : null,
                pageable);
        return new PageResponse<>(articleMapper.toResponseList(result.getContent()),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private String resolveSlugCandidate(String slug, String title) {
        if (StringUtils.hasText(slug)) {
            return slug.toLowerCase(Locale.ROOT);
        }
        if (!StringUtils.hasText(title)) {
            throw new BadRequestException("Невозможно сгенерировать slug без заголовка");
        }
        return SlugGenerator.fromText(title);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        List<String> normalized = new ArrayList<>();
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String trimmed = tag.trim();
            normalized.add(trimmed);
        }
        if (normalized.size() > 20) {
            throw new BadRequestException("Максимум 20 тегов");
        }
        return new ArrayList<>(normalized);
    }

    private String generateUniqueSlug(String desired, UUID currentId) {
        if (!StringUtils.hasText(desired)) {
            throw new BadRequestException("Slug не может быть пустым");
        }
        String base = desired;
        if (base.length() > MAX_SLUG_LENGTH) {
            base = base.substring(0, MAX_SLUG_LENGTH);
        }
        String candidate = base;
        int suffix = 1;
        while (true) {
            Optional<ArticleEntity> existing = articleRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(currentId)) {
                return candidate;
            }
            suffix++;
            String suffixPart = "-" + suffix;
            int maxBaseLength = MAX_SLUG_LENGTH - suffixPart.length();
            String truncatedBase = base.length() > maxBaseLength ? base.substring(0, maxBaseLength) : base;
            candidate = truncatedBase + suffixPart;
        }
    }
}
