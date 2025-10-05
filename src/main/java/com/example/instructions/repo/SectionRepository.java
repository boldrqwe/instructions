package com.example.instructions.repo;

import com.example.instructions.domain.Section;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий секций.
 */
public interface SectionRepository extends JpaRepository<Section, UUID> {

    List<Section> findByChapterIdOrderByOrderIndexAsc(UUID chapterId);
}
