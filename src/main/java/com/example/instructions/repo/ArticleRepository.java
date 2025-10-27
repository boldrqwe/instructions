package com.example.instructions.repo;

import com.example.instructions.domain.Article;
import com.example.instructions.domain.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @Query("""
        select distinct a
        from Article a
        left join fetch a.tagEntities t
        left join fetch a.chapters c
        left join fetch c.sections s
        where a.slug = :slug and a.status = :status
    """)
    Optional<Article> findDetailedBySlugAndStatus(@Param("slug") String slug,
                                                  @Param("status") ArticleStatus status);

    @Query("""
        select distinct a
        from Article a
        left join fetch a.chapters c
        left join fetch c.sections s
        where a.id = :id and a.status = :status
    """)
    Optional<Article> findDetailedByIdAndStatus(@Param("id") UUID id,
                                                @Param("status") ArticleStatus status);


    @Query(
            value = """
        SELECT * FROM article a
        WHERE (COALESCE(CAST(:status AS TEXT), '') = '' OR a.status = CAST(:status AS TEXT))
          AND (COALESCE(TRIM(:query), '') = '' OR a.search_vector @@ plainto_tsquery('simple', :query))
        ORDER BY a.updated_at DESC
        """,
            countQuery = """
        SELECT count(*) FROM article a
        WHERE (COALESCE(CAST(:status AS TEXT), '') = '' OR a.status = CAST(:status AS TEXT))
          AND (COALESCE(TRIM(:query), '') = '' OR a.search_vector @@ plainto_tsquery('simple', :query))
        """,
            nativeQuery = true
    )
    Page<Article> search(@Param("status") ArticleStatus status,
                         @Param("query") String query,
                         Pageable pageable);

    Set<Article> findAllByStatus(ArticleStatus status);

}
