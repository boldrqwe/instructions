package com.boldin.instructions.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstructionRepository extends JpaRepository<Instruction, UUID> {
}
