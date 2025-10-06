package com.example.instructions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.instructions.AbstractIntegrationTest;
import com.example.instructions.api.dto.ArticleDto;
import com.example.instructions.api.dto.SearchResultDto;
import com.example.instructions.common.ConflictException;
import com.example.instructions.common.NotFoundException;
import com.example.instructions.common.PageResponse;
import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.domain.Chapter;
import com.example.instructions.domain.Section;
import com.example.instructions.repo.ArticleRepository;
import com.example.instructions.repo.ChapterRepository;
import com.example.instructions.repo.RevisionRepository;
import com.example.instructions.repo.SectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Интеграционные сценарии публикации и поиска.
 */
class PublicationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private RevisionRepository revisionRepository;

    @Test
    @DisplayName("[METHOD: publishArticle()] - OK")
    void publishDraftSuccess() {
        Article draft = createDraft("MVP Draft", "mvp-draft");
        Chapter chapter = createChapter(draft, "Chapter A", 0);
        createSection(chapter, "Section A", 0, "Draft content with postgres tips");

        ArticleDto published = publicationService.publishArticle(draft.getId());

        assertThat(published.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(published.getVersion()).isEqualTo(2);
        assertThat(revisionRepository.count()).isGreaterThan(0);

        PageResponse<SearchResultDto> searchResults = searchService.search("postgres", 0, 10);
        assertThat(searchResults.getContent()).extracting(SearchResultDto::getSlug)
                .contains(published.getSlug());
    }

    @Test
    @DisplayName("[METHOD: publishArticle()] - ERROR")
    void publishAlreadyPublished() {
        Article published = articleRepository.findBySlugAndStatus("welcome", ArticleStatus.PUBLISHED)
                .orElseThrow();
        assertThatThrownBy(() -> publicationService.publishArticle(published.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("[METHOD: getPublishedBySlug()] - OK")
    void getPublishedBySlug() {
        ArticleDto article = articleService.getPublishedArticle("welcome");
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(article.getChapters()).isNotEmpty();
    }

    @Test
    @DisplayName("[METHOD: getPublishedBySlug()] - ERROR")
    void getDraftBySlugShouldFail() {
        Article draft = createDraft("Hidden Draft", "hidden-draft");
        assertThatThrownBy(() -> articleService.getPublishedArticle(draft.getSlug()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("[METHOD: searchPublished()] - OK")
    void searchPublishedArticlesAndSections() {
        PageResponse<SearchResultDto> results = searchService.search("welcome", 0, 10);
        assertThat(results.getContent()).isNotEmpty();
    }

    private Article createDraft(String title, String slug) {
        Article article = new Article();
        article.setTitle(title);
        article.setSlug(slug);
        article.setStatus(ArticleStatus.DRAFT);
        article.setCreatedBy("tester");
        return articleRepository.save(article);
    }

    private Chapter createChapter(Article article, String title, int orderIndex) {
        Chapter chapter = new Chapter();
        chapter.setArticle(article);
        chapter.setTitle(title);
        chapter.setOrderIndex(orderIndex);
        return chapterRepository.save(chapter);
    }

    private Section createSection(Chapter chapter, String title, int orderIndex, String markdown) {
        Section section = new Section();
        section.setChapter(chapter);
        section.setTitle(title);
        section.setOrderIndex(orderIndex);
        section.setMarkdown(markdown);
        return sectionRepository.save(section);
    }
}
