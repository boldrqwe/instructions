package com.example.instructions.repo;

import com.example.instructions.domain.Chapter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий глав.
 */
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {

    List<Chapter> findByArticleIdOrderByOrderIndexAsc(UUID articleId);
}
