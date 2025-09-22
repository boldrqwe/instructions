package com.boldin.instructions.web;

import com.boldin.instructions.domain.Instruction;
import com.boldin.instructions.domain.InstructionResource;
import com.boldin.instructions.domain.InstructionSection;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record InstructionResponse(
        UUID id,
        String slug,
        String title,
        String summary,
        String introduction,
        String difficulty,
        int estimatedMinutes,
        String prerequisites,
        InstructionCategoryResponse category,
        List<String> tags,
        List<InstructionSectionResponse> sections,
        List<InstructionResourceResponse> resources,
        Instant createdAt,
        Instant updatedAt
) {

    public static InstructionResponse fromEntity(Instruction instruction) {
        return new InstructionResponse(
                instruction.getId(),
                instruction.getSlug(),
                instruction.getTitle(),
                instruction.getSummary(),
                instruction.getIntroduction(),
                instruction.getDifficulty() != null ? instruction.getDifficulty().getValue() : null,
                instruction.getEstimatedMinutes(),
                instruction.getPrerequisites(),
                InstructionCategoryResponse.fromEntity(instruction.getCategory()),
                List.copyOf(instruction.getTags()),
                instruction.getSections().stream()
                        .sorted(Comparator.comparingInt(InstructionSection::getPosition))
                        .map(InstructionSectionResponse::fromEntity)
                        .toList(),
                instruction.getResources().stream()
                        .sorted(Comparator.comparingInt(InstructionResource::getPosition))
                        .map(InstructionResourceResponse::fromEntity)
                        .toList(),
                instruction.getCreatedAt(),
                instruction.getUpdatedAt()
        );
    }

    public record InstructionCategoryResponse(
            UUID id,
            String slug,
            String title,
            String description,
            String icon
    ) {
        public static InstructionCategoryResponse fromEntity(com.boldin.instructions.domain.InstructionCategory category) {
            return new InstructionCategoryResponse(
                    category.getId(),
                    category.getSlug(),
                    category.getTitle(),
                    category.getDescription(),
                    category.getIcon()
            );
        }
    }

    public record InstructionSectionResponse(
            UUID id,
            int position,
            String title,
            String content,
            String codeTitle,
            String codeLanguage,
            String codeSnippet,
            String ctaLabel,
            String ctaUrl
    ) {
        public static InstructionSectionResponse fromEntity(InstructionSection section) {
            return new InstructionSectionResponse(
                    section.getId(),
                    section.getPosition(),
                    section.getTitle(),
                    section.getContent(),
                    section.getCodeTitle(),
                    section.getCodeLanguage(),
                    section.getCodeSnippet(),
                    section.getCtaLabel(),
                    section.getCtaUrl()
            );
        }
    }

    public record InstructionResourceResponse(
            UUID id,
            int position,
            String type,
            String title,
            String description,
            String url
    ) {
        public static InstructionResourceResponse fromEntity(InstructionResource resource) {
            return new InstructionResourceResponse(
                    resource.getId(),
                    resource.getPosition(),
                    resource.getType(),
                    resource.getTitle(),
                    resource.getDescription(),
                    resource.getUrl()
            );
        }
    }
}
