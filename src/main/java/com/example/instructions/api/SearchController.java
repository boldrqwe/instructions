package com.example.instructions.api;

import com.example.instructions.common.PageResponse;
import com.example.instructions.service.SearchService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Публичный контроллер поиска.
 */
@RestController
@RequestMapping("/api/v1/search")
@Validated
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public PageResponse<SearchResultDto> search(
            @RequestParam("query") @NotBlank String query,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Positive int size) {
        return searchService.search(query, page, size);
    }
}
