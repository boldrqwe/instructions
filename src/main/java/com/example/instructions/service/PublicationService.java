package com.example.instructions.service;

import com.example.instructions.api.ArticleDto;
import com.example.instructions.api.ArticleMapper;
import com.example.instructions.common.ConflictException;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.domain.Revision;
import com.example.instructions.repo.ArticleRepository;
import com.example.instructions.repo.RevisionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис публикации статей с созданием ревизий.
 */
@Service
public class PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);

    private final ArticleRepository articleRepository;
    private final RevisionRepository revisionRepository;
    private final ArticleMapper articleMapper;
    private final ObjectMapper objectMapper;

    public PublicationService(ArticleRepository articleRepository,
                              RevisionRepository revisionRepository,
                              ArticleMapper articleMapper,
                              ObjectMapper objectMapper) {
        this.articleRepository = articleRepository;
        this.revisionRepository = revisionRepository;
        this.articleMapper = articleMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Публикует черновик статьи и создаёт ревизию.
     *
     * @param articleId идентификатор статьи
     * @return опубликованная статья
     */
    @Transactional
    public ArticleDto publishArticle(UUID articleId) {
        Article article = articleRepository.findDetailedById(articleId)
                .orElseThrow(() -> new NotFoundException("Статья не найдена"));
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new ConflictException("Публиковать можно только черновики");
        }
        Optional<Article> existing = articleRepository.findBySlugAndStatus(article.getSlug(), ArticleStatus.PUBLISHED);
        if (existing.isPresent() && !existing.get().getId().equals(article.getId())) {
            throw new ConflictException("Опубликованная статья с таким слагом уже существует");
        }
        article.getChapters().forEach(chapter -> chapter.getSections().size());
        article.setVersion(article.getVersion() + 1);
        article.setStatus(ArticleStatus.PUBLISHED);
        ArticleDto snapshot = articleMapper.toDto(article);
        String snapshotJson = toJson(snapshot);
        Revision revision = new Revision();
        revision.setArticle(article);
        revision.setVersion(article.getVersion());
        revision.setSnapshot(snapshotJson);
        revisionRepository.save(revision);
        Article saved = articleRepository.save(article);
        log.info("[Publication] articleId={} version={}", saved.getId(), saved.getVersion());
        return articleMapper.toDto(saved);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать снимок статьи", e);
        }
    }
}
