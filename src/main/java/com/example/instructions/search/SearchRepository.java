package com.example.instructions.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Репозиторий полнотекстового поиска.
 */
public interface SearchRepository {

    Page<SearchResultProjection> search(String query, Pageable pageable);
}
