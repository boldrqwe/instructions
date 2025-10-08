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
import org.springframework.data.repository.query.Param;

/**
 * Репозиторий статей.
 */
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    @EntityGraph(attributePaths = {"tagEntities"})
    Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"tagEntities"})
    Page<Article> findByStatusAndTitleContainingIgnoreCase(ArticleStatus status, String title, Pageable pageable);

    @EntityGraph(attributePaths = {"tagEntities", "chapters", "chapters.sections"})
    Optional<Article> findById(UUID id);

    @EntityGraph(attributePaths = {"tagEntities", "chapters", "chapters.sections"})
    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);

    @EntityGraph(attributePaths = {"tagEntities", "chapters", "chapters.sections"})
    Optional<Article> findBySlug(String slug);

    @EntityGraph(attributePaths = {"tagEntities", "chapters", "chapters.sections"})
    @Query("select a from Article a where a.id = :id")
    Optional<Article> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {"tagEntities", "chapters", "chapters.sections"})
    @Query("select a from Article a where a.id = :id and a.status = :status")
    Optional<Article> findDetailedByIdAndStatus(UUID id, ArticleStatus status);

    @Query(
            value = """
       SELECT * FROM article a
       WHERE (:status IS NULL OR a.status = CAST(:status AS VARCHAR))
         AND (:query IS NULL OR trim(:query) = '' OR a.search_vector @@ plainto_tsquery('simple', :query))
       ORDER BY a.updated_at DESC
       """,
            countQuery = """
       SELECT count(*) FROM article a
       WHERE (:status IS NULL OR a.status = CAST(:status AS VARCHAR))
         AND (:query IS NULL OR trim(:query) = '' OR a.search_vector @@ plainto_tsquery('simple', :query))
       """,
            nativeQuery = true
    )
    Page<Article> search(@Param("status") ArticleStatus status,
                         @Param("query") String query,
                         Pageable pageable);
}
