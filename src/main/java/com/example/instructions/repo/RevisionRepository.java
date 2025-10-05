package com.example.instructions.repo;

import com.example.instructions.domain.Revision;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий ревизий статей.
 */
public interface RevisionRepository extends JpaRepository<Revision, UUID> {
}
