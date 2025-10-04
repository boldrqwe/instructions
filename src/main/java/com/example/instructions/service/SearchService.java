package com.example.instructions.service;

import com.example.instructions.api.SearchResultDto;
import com.example.instructions.api.SearchResultType;
import com.example.instructions.common.PageResponse;
import com.example.instructions.search.SearchRepository;
import com.example.instructions.search.SearchResultProjection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Сервис полнотекстового поиска по опубликованному контенту.
 */
@Service
public class SearchService {

    private static final Pattern MARKDOWN_PATTERN = Pattern.compile("[#*_`>\\[\\]]");

    private final SearchRepository searchRepository;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    /**
     * Выполняет поиск среди опубликованных статей и секций.
     *
     * @param query текст запроса
     * @param page  номер страницы
     * @param size  размер страницы
     * @return страница результатов
     */
    public PageResponse<SearchResultDto> search(String query, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
        Page<SearchResultProjection> results = searchRepository.search(query, PageRequest.of(page, size));
        List<SearchResultDto> content = results.getContent().stream()
                .map(projection -> {
                    SearchResultDto dto = new SearchResultDto();
                    dto.setId(projection.getId());
                    dto.setSlug(projection.getSlug());
                    dto.setTitle(projection.getTitle());
                    dto.setType(SearchResultType.valueOf(projection.getType()));
                    dto.setSnippet(buildSnippet(projection, query));
                    return dto;
                })
                .toList();
        return new PageResponse<>(content, results.getNumber(), results.getSize(), results.getTotalElements());
    }

    private String buildSnippet(SearchResultProjection projection, String query) {
        String raw = projection.getMarkdown() != null ? projection.getMarkdown() : projection.getTitle();
        String sanitized = MARKDOWN_PATTERN.matcher(raw).replaceAll(" ");
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        if (sanitized.length() <= 200) {
            return sanitized;
        }
        String lower = sanitized.toLowerCase(Locale.ROOT);
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        int index = lower.indexOf(lowerQuery);
        if (index < 0) {
            return sanitized.substring(0, 200).trim() + "...";
        }
        int start = Math.max(0, index - 60);
        int end = Math.min(sanitized.length(), index + lowerQuery.length() + 60);
        return sanitized.substring(start, end).trim() + "...";
    }
}
