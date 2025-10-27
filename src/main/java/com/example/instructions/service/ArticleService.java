package com.example.instructions.service;

import com.example.instructions.api.dto.ArticleDto;
import com.example.instructions.api.dto.TocDto;
import com.example.instructions.api.mapper.ArticleMapper;
import com.example.instructions.api.mapper.TocMapper;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.repo.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Прикладной сервис для работы со статьями и их оглавлением.
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final TocMapper tocMapper;


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
        return articleMapper.toPublicDto(article);
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
        TocDto tocDto = tocMapper.toTocDto(article.getId(), article.getChapters());
        return tocDto;
    }
}
