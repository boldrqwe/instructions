package com.example.instructions.repo;

import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Репозиторий статей.
 */
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    @EntityGraph(attributePaths = {"tags"})
    Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"tags"})
    Page<Article> findByStatusAndTitleContainingIgnoreCase(ArticleStatus status, String title, Pageable pageable);

    @EntityGraph(attributePaths = {"tags", "chapters", "chapters.sections"})
    Optional<Article> findById(UUID id);

    @EntityGraph(attributePaths = {"tags", "chapters", "chapters.sections"})
    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);

    @EntityGraph(attributePaths = {"tags", "chapters", "chapters.sections"})
    @Query("select a from Article a where a.id = :id")
    Optional<Article> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {"tags", "chapters", "chapters.sections"})
    @Query("select a from Article a where a.id = :id and a.status = :status")
    Optional<Article> findDetailedByIdAndStatus(UUID id, ArticleStatus status);
}
