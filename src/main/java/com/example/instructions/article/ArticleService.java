package com.example.instructions.article;

import com.example.instructions.article.dto.ArticleRequest;
import com.example.instructions.article.dto.ArticleResponse;
import com.example.instructions.article.exception.ArticleNotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> findAll() {
        return articleRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .map(ArticleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse findById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
        return ArticleMapper.toResponse(article);
    }

    public ArticleResponse create(ArticleRequest request) {
        Article article = ArticleMapper.toEntity(request);
        Article saved = articleRepository.save(article);
        return ArticleMapper.toResponse(saved);
    }

    public ArticleResponse update(Long id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
        ArticleMapper.updateEntity(article, request);
        Article saved = articleRepository.save(article);
        return ArticleMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
        articleRepository.delete(article);
    }
}
