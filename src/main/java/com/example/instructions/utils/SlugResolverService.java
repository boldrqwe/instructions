package com.example.instructions.utils;

import com.example.instructions.common.BadRequestException;
import com.example.instructions.common.SlugGenerator;
import com.example.instructions.domain.Article;
import com.example.instructions.repo.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlugResolverService {

    private final ArticleRepository articleRepository;

    public String resolveSlug(String providedSlug, String title, UUID currentId) {
        String baseSlug;
        if (StringUtils.hasText(providedSlug)) {
            baseSlug = providedSlug.trim().toLowerCase(Locale.ROOT);
        } else if (StringUtils.hasText(title)) {
            baseSlug = SlugGenerator.fromText(title);
        } else {
            throw new BadRequestException("Slug or title must be provided");
        }
        if (!baseSlug.matches("^[a-z0-9-]+$")) {
            throw new BadRequestException("Недопустимый slug");
        }
        String candidate = baseSlug;
        int counter = 2;
        while (true) {
            Optional<Article> existing = articleRepository.findBySlug(candidate);
            if (existing.isEmpty() || (currentId != null && existing.get().getId().equals(currentId))) {
                return candidate;
            }
            candidate = baseSlug + "-" + counter++;
        }
    }
}
