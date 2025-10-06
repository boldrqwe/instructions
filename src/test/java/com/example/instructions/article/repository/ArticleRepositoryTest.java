package com.example.instructions.article.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.instructions.PostgresIntegrationTest;
import com.example.instructions.article.model.ArticleEntity;
import com.example.instructions.article.model.ArticleStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Тесты репозитория статей.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest extends PostgresIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
    }

    @Test
    void shouldSaveWithoutExplicitSearchVector() {
        ArticleEntity entity = buildArticle("Test article", "test-article");
        ArticleEntity saved = articleRepository.save(entity);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldSearchByHtmlContent() {
        ArticleEntity first = buildArticle("Postgres tips", "postgres-tips");
        first.setContentHtml("<p>Настройка PostgreSQL и индексы</p>");
        first.setStatus(ArticleStatus.PUBLISHED);
        articleRepository.save(first);

        ArticleEntity second = buildArticle("Other", "other");
        second.setContentHtml("<p>Советы по Redis</p>");
        second.setStatus(ArticleStatus.PUBLISHED);
        articleRepository.save(second);

        articleRepository.flush();

        Page<ArticleEntity> page = articleRepository.search(ArticleStatus.PUBLISHED.name(), "PostgreSQL", null,
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        List<ArticleEntity> content = page.getContent();
        assertThat(content).hasSize(1);
        assertThat(content.get(0).getSlug()).isEqualTo("postgres-tips");
    }

    private ArticleEntity buildArticle(String title, String slug) {
        ArticleEntity entity = new ArticleEntity();
        entity.setTitle(title);
        entity.setSlug(slug);
        entity.setStatus(ArticleStatus.DRAFT);
        entity.setContentHtml("<p>Content</p>");
        entity.setContentJson(OBJECT_MAPPER.createObjectNode());
        entity.setSummary("Summary");
        entity.setCreatedBy("tester");
        entity.setTags(List.of("db"));
        entity.setCoverImageUrl(null);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }
}
