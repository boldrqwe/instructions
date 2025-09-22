package com.boldin.instructions.web;

import com.boldin.instructions.domain.Instruction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InstructionSummaryResponse(
        UUID id,
        String slug,
        String title,
        String summary,
        String difficulty,
        int estimatedMinutes,
        InstructionResponse.InstructionCategoryResponse category,
        List<String> tags,
        Instant updatedAt
) {

    public static InstructionSummaryResponse fromEntity(Instruction instruction) {
        return new InstructionSummaryResponse(
                instruction.getId(),
                instruction.getSlug(),
                instruction.getTitle(),
                instruction.getSummary(),
                instruction.getDifficulty() != null ? instruction.getDifficulty().getValue() : null,
                instruction.getEstimatedMinutes(),
                InstructionResponse.InstructionCategoryResponse.fromEntity(instruction.getCategory()),
                List.copyOf(instruction.getTags()),
                instruction.getUpdatedAt()
        );
    }
}
