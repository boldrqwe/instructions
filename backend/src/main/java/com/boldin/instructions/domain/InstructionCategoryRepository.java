package com.boldin.instructions.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstructionCategoryRepository extends JpaRepository<InstructionCategory, UUID> {

    Optional<InstructionCategory> findBySlugIgnoreCase(String slug);
}
