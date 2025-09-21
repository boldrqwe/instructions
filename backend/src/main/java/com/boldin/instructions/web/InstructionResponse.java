package com.boldin.instructions.web;

import com.boldin.instructions.domain.Instruction;

import java.time.Instant;
import java.util.UUID;

public record InstructionResponse(
        UUID id,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static InstructionResponse fromEntity(Instruction instruction) {
        return new InstructionResponse(
                instruction.getId(),
                instruction.getTitle(),
                instruction.getContent(),
                instruction.getCreatedAt(),
                instruction.getUpdatedAt()
        );
    }
}
