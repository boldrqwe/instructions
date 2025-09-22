package com.example.instructions.article;

import com.example.instructions.article.dto.ArticleRequest;
import com.example.instructions.article.dto.ArticleResponse;

public final class ArticleMapper {

    private ArticleMapper() {
    }

    public static Article toEntity(ArticleRequest request) {
        return new Article(request.title().trim(), request.content());
    }

    public static void updateEntity(Article article, ArticleRequest request) {
        article.setTitle(request.title().trim());
        article.setContent(request.content());
    }

    public static ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
