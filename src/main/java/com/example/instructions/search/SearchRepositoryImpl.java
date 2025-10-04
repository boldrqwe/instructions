package com.example.instructions.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория поиска с использованием PostgreSQL tsvector.
 */
@Repository
public class SearchRepositoryImpl implements SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<SearchResultProjection> search(String query, Pageable pageable) {
        String sanitized = Objects.requireNonNullElse(query, "").trim();
        if (sanitized.isEmpty()) {
            return Page.empty(pageable);
        }
        String baseSelect =
                "WITH search_query AS (SELECT plainto_tsquery('simple', :query) AS q) "
                        + "SELECT type, id, title, markdown, slug, rank FROM ("
                        + "SELECT 'article' AS type, a.id, a.title, NULL AS markdown, a.slug, "
                        + "ts_rank_cd(a.search_vector, sq.q) AS rank "
                        + "FROM article a, search_query sq "
                        + "WHERE a.status = 'PUBLISHED' AND a.search_vector @@ sq.q "
                        + "UNION ALL "
                        + "SELECT 'section' AS type, s.id, s.title, s.markdown, a.slug, "
                        + "ts_rank_cd(s.search_vector, sq.q) AS rank "
                        + "FROM section s "
                        + "JOIN chapter c ON s.chapter_id = c.id "
                        + "JOIN article a ON c.article_id = a.id, search_query sq "
                        + "WHERE a.status = 'PUBLISHED' AND s.search_vector @@ sq.q"
                        + ") AS combined ORDER BY rank DESC, title ASC";

        Query nativeQuery = entityManager.createNativeQuery(baseSelect)
                .setParameter("query", sanitized)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        List<SearchResultProjection> projections = new ArrayList<>();
        for (Object[] row : rows) {
            projections.add(new SearchResultProjection(
                    (String) row[0],
                    java.util.UUID.fromString(row[1].toString()),
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    ((Number) row[5]).doubleValue()
            ));
        }

        String countSql =
                "WITH search_query AS (SELECT plainto_tsquery('simple', :query) AS q) "
                        + "SELECT COUNT(1) FROM ("
                        + "SELECT a.id FROM article a, search_query sq "
                        + "WHERE a.status = 'PUBLISHED' AND a.search_vector @@ sq.q "
                        + "UNION ALL "
                        + "SELECT s.id FROM section s "
                        + "JOIN chapter c ON s.chapter_id = c.id "
                        + "JOIN article a ON c.article_id = a.id, search_query sq "
                        + "WHERE a.status = 'PUBLISHED' AND s.search_vector @@ sq.q"
                        + ") AS counted";

        Number total = (Number) entityManager.createNativeQuery(countSql)
                .setParameter("query", sanitized)
                .getSingleResult();

        return new PageImpl<>(projections, pageable, total.longValue());
    }
}
