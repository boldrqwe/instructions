package com.example.instructions.article;

import com.example.instructions.article.dto.ArticleRequest;
import com.example.instructions.article.dto.ArticleResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@Validated
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleResponse> getAll() {
        return articleService.findAll();
    }

    @GetMapping("/{id}")
    public ArticleResponse getById(@PathVariable Long id) {
        return articleService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@RequestBody @Valid ArticleRequest request) {
        ArticleResponse response = articleService.create(request);
        return ResponseEntity.created(URI.create("/api/articles/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ArticleResponse update(@PathVariable Long id, @RequestBody @Valid ArticleRequest request) {
        return articleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
