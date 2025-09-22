package com.boldin.instructions.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructionRepository extends JpaRepository<Instruction, UUID> {

    @Query("select distinct i from Instruction i " +
            "left join fetch i.category " +
            "left join fetch i.sections " +
            "left join fetch i.resources " +
            "left join fetch i.tags")
    List<Instruction> findAllDetailed();

    @Query("select distinct i from Instruction i " +
            "left join fetch i.category " +
            "left join fetch i.sections " +
            "left join fetch i.resources " +
            "left join fetch i.tags " +
            "where i.id = :id")
    Optional<Instruction> findByIdDetailed(@Param("id") UUID id);

    @Query("select distinct i from Instruction i " +
            "left join fetch i.category " +
            "left join fetch i.sections " +
            "left join fetch i.resources " +
            "left join fetch i.tags " +
            "where lower(i.slug) = lower(:slug)")
    Optional<Instruction> findBySlugDetailed(@Param("slug") String slug);
}
