package com.example.instructions.service;

import com.example.instructions.api.model.ArticleDraftCreateRequest;
import com.example.instructions.api.model.ArticleDraftUpdateRequest;
import com.example.instructions.api.dto.ArticleDto;
import com.example.instructions.api.mapper.ArticleMapper;
import com.example.instructions.api.dto.ArticleSummaryDto;
import com.example.instructions.api.model.ChapterCreateRequest;
import com.example.instructions.api.dto.ChapterDto;
import com.example.instructions.api.model.SectionCreateRequest;
import com.example.instructions.api.dto.SectionDto;
import com.example.instructions.api.dto.TocDto;
import com.example.instructions.api.mapper.TocMapper;
import com.example.instructions.common.BadRequestException;
import com.example.instructions.common.ConflictException;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.common.PageResponse;
import com.example.instructions.common.SlugGenerator;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.domain.Chapter;
import com.example.instructions.domain.Section;
import com.example.instructions.domain.Tag;
import com.example.instructions.repo.ArticleRepository;
import com.example.instructions.repo.ChapterRepository;
import com.example.instructions.repo.SectionRepository;
import com.example.instructions.repo.TagRepository;
import com.example.instructions.security.AuthenticationFacade;
import jakarta.transaction.Transactional;
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
 * Прикладной сервис для работы со статьями и их оглавлением.
 */
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;
    private final TagRepository tagRepository;
    private final ArticleMapper articleMapper;
    private final TocMapper tocMapper;
    private final AuthenticationFacade authenticationFacade;

    public ArticleService(ArticleRepository articleRepository,
                          ChapterRepository chapterRepository,
                          SectionRepository sectionRepository,
                          TagRepository tagRepository,
                          ArticleMapper articleMapper,
                          TocMapper tocMapper,
                          AuthenticationFacade authenticationFacade) {
        this.articleRepository = articleRepository;
        this.chapterRepository = chapterRepository;
        this.sectionRepository = sectionRepository;
        this.tagRepository = tagRepository;
        this.articleMapper = articleMapper;
        this.tocMapper = tocMapper;
        this.authenticationFacade = authenticationFacade;
    }

    /**
     * Возвращает страницу статей по статусу и текстовому фильтру.
     *
     * @param status статус (по умолчанию PUBLISHED)
     * @param query  строка поиска по заголовку
     * @param page   номер страницы
     * @param size   размер страницы
     * @return страница статей
     */
    public PageResponse<ArticleSummaryDto> getArticles(ArticleStatus status, String query, int page, int size) {
        ArticleStatus effectiveStatus = status != null ? status : ArticleStatus.PUBLISHED;
        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles;
        if (StringUtils.hasText(query)) {
            articles = articleRepository.findByStatusAndTitleContainingIgnoreCase(effectiveStatus, query, pageable);
        } else {
            articles = articleRepository.findAllByStatus(effectiveStatus, pageable);
        }
        List<ArticleSummaryDto> content = articleMapper.toSummaryList(articles.getContent());
        return new PageResponse<>(content, articles.getNumber(), articles.getSize(), articles.getTotalElements());
    }

    /**
     * Загружает опубликованную статью по слагу.
     *
     * @param slug уникальный слаг
     * @return DTO статьи
     */
    public ArticleDto getPublishedArticle(String slug) {
        Article article = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Статья не найдена или не опубликована"));
        article.getChapters().forEach(chapter -> chapter.getSections().size());
        return articleMapper.toDto(article);
    }

    /**
     * Возвращает оглавление опубликованной статьи.
     *
     * @param articleId идентификатор статьи
     * @return оглавление
     */
    public TocDto getPublishedToc(UUID articleId) {
        Article article = articleRepository.findDetailedByIdAndStatus(articleId, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Оглавление доступно только для опубликованных статей"));
        return tocMapper.toTocDto(article.getId(), article.getChapters());
    }

    /**
     * Создаёт новый черновик статьи.
     *
     * @param request параметры создания
     * @return созданная статья
     */
    @Transactional
    public ArticleDto createDraft(ArticleDraftCreateRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        String slug = StringUtils.hasText(request.getSlug()) ? request.getSlug() : SlugGenerator.fromText(request.getTitle());
        article.setSlug(validateSlug(slug));
        article.setStatus(ArticleStatus.DRAFT);
        article.setCreatedBy(authenticationFacade.getCurrentUserId());
        article.setTags(resolveTags(request.getTags()));
        Article saved = articleRepository.save(article);
        return articleMapper.toDto(saved);
    }

    /**
     * Обновляет черновик статьи.
     *
     * @param id      идентификатор
     * @param request параметры обновления
     * @return обновлённая статья
     */
    @Transactional
    public ArticleDto updateDraft(UUID id, ArticleDraftUpdateRequest request) {
        Article article = loadArticle(id);
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new ConflictException("Изменять можно только черновики");
        }
        if (StringUtils.hasText(request.getTitle())) {
            article.setTitle(request.getTitle());
        }
        if (request.getSlug() != null) {
            String slug = StringUtils.hasText(request.getSlug())
                    ? validateSlug(request.getSlug())
                    : SlugGenerator.fromText(Optional.ofNullable(request.getTitle()).orElse(article.getTitle()));
            article.setSlug(slug);
        }
        if (request.getTags() != null) {
            article.setTags(resolveTags(request.getTags()));
        }
        Article saved = articleRepository.save(article);
        return articleMapper.toDto(saved);
    }

    /**
     * Создаёт главу для черновика статьи.
     *
     * @param request запрос на создание
     * @return созданная глава
     */
    @Transactional
    public ChapterDto createChapter(ChapterCreateRequest request) {
        Article article = loadArticle(request.getArticleId());
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new ConflictException("Главы можно изменять только в черновике");
        }
        Chapter chapter = new Chapter();
        chapter.setArticle(article);
        chapter.setOrderIndex(request.getOrderIndex());
        chapter.setTitle(request.getTitle());
        Chapter saved = chapterRepository.save(chapter);
        return articleMapper.toChapterDto(saved);
    }

    /**
     * Создаёт секцию в главе черновика.
     *
     * @param request запрос
     * @return созданная секция
     */
    @Transactional
    public SectionDto createSection(SectionCreateRequest request) {
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new NotFoundException("Глава не найдена"));
        if (chapter.getArticle().getStatus() != ArticleStatus.DRAFT) {
            throw new ConflictException("Секции можно изменять только в черновике");
        }
        Section section = new Section();
        section.setChapter(chapter);
        section.setOrderIndex(request.getOrderIndex());
        section.setTitle(request.getTitle());
        section.setMarkdown(request.getMarkdown());
        Section saved = sectionRepository.save(section);
        return articleMapper.toSectionDto(saved);
    }

    private Article loadArticle(UUID id) {
        return articleRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
    }

    private java.util.Set<Tag> resolveTags(List<String> tagNames) {
        java.util.Set<Tag> tags = new java.util.LinkedHashSet<>();
        if (tagNames == null) {
            return tags;
        }
        for (String tagName : tagNames) {
            if (!StringUtils.hasText(tagName)) {
                continue;
            }
            String slug = SlugGenerator.fromText(tagName).toLowerCase(Locale.ROOT);
            Tag tag = tagRepository.findBySlug(slug)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName.trim());
                        newTag.setSlug(slug);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    private String validateSlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new BadRequestException("Слаг не может быть пустым");
        }
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}
