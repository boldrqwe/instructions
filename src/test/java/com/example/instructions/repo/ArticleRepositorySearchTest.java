package com.example.instructions.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.instructions.AbstractIntegrationTest;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class ArticleRepositorySearchTest extends AbstractIntegrationTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void searchByFullTextVector() {
        Article article = new Article();
        article.setId(UUID.randomUUID());
        article.setTitle("Postgres Tips");
        article.setSlug("postgres-tips");
        article.setSummary("Advanced Postgres search tricks");
        article.setContentHtml("<p>Postgres full-text search</p>");
        article.setContentJson(JsonNodeFactory.instance.objectNode());
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setCreatedBy("tester");
        articleRepository.saveAndFlush(article);

        Page<Article> result = articleRepository.search(ArticleStatus.PUBLISHED, "Postgres", PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Article::getSlug)
                .contains("postgres-tips");
    }
}
