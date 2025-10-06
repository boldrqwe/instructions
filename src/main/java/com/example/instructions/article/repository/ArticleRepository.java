package com.example.instructions.article.repository;

import com.example.instructions.article.model.ArticleEntity;
import com.example.instructions.article.model.ArticleStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Репозиторий статей.
 */
public interface ArticleRepository extends JpaRepository<ArticleEntity, UUID> {

    Optional<ArticleEntity> findBySlug(String slug);

    Optional<ArticleEntity> findBySlugAndStatus(String slug, ArticleStatus status);

    boolean existsBySlug(String slug);

    Optional<ArticleEntity> findByIdAndStatus(UUID id, ArticleStatus status);

    @Query(value = """
            SELECT *
            FROM article
            WHERE (:status IS NULL OR status = :status)
              AND (:authorId IS NULL OR created_by = :authorId)
              AND (:query IS NULL OR search_vector @@ plainto_tsquery('russian', :query))
            ORDER BY updated_at DESC
            """,
            countQuery = """
                    SELECT count(*)
                    FROM article
                    WHERE (:status IS NULL OR status = :status)
                      AND (:authorId IS NULL OR created_by = :authorId)
                      AND (:query IS NULL OR search_vector @@ plainto_tsquery('russian', :query))
                    """,
            nativeQuery = true)
    Page<ArticleEntity> search(@Param("status") String status,
                               @Param("query") String query,
                               @Param("authorId") String authorId,
                               Pageable pageable);
}
