package com.boldin.instructions.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InstructionRequest(
        @NotBlank(message = "Slug is required")
        @Size(max = 120, message = "Slug must not exceed 120 characters")
        String slug,
        @NotBlank(message = "Title is required")
        @Size(max = 180, message = "Title must not exceed 180 characters")
        String title,
        @NotBlank(message = "Summary is required")
        @Size(max = 512, message = "Summary must not exceed 512 characters")
        String summary,
        @NotBlank(message = "Introduction is required")
        @Size(max = 8192, message = "Introduction must not exceed 8192 characters")
        String introduction,
        @NotBlank(message = "Difficulty is required")
        String difficulty,
        @Min(value = 1, message = "Estimated minutes must be at least 1")
        int estimatedMinutes,
        @NotBlank(message = "Category is required")
        @Size(max = 120, message = "Category slug must not exceed 120 characters")
        String categorySlug,
        @Size(max = 1024, message = "Prerequisites must not exceed 1024 characters")
        String prerequisites,
        @NotNull(message = "Tags must be provided")
        List<@NotBlank(message = "Tag must not be blank") @Size(max = 64, message = "Tag must not exceed 64 characters") String> tags,
        @NotEmpty(message = "At least one section is required")
        List<@Valid InstructionSectionRequest> sections,
        @NotNull(message = "Resources list must be provided")
        List<@Valid InstructionResourceRequest> resources
) {

    public record InstructionSectionRequest(
            @NotBlank(message = "Section title is required")
            @Size(max = 180, message = "Section title must not exceed 180 characters")
            String title,
            @NotBlank(message = "Section content is required")
            @Size(max = 8192, message = "Section content must not exceed 8192 characters")
            String content,
            @Size(max = 180, message = "Code title must not exceed 180 characters")
            String codeTitle,
            @Size(max = 32, message = "Code language must not exceed 32 characters")
            String codeLanguage,
            @Size(max = 8192, message = "Code snippet must not exceed 8192 characters")
            String codeSnippet,
            @Size(max = 120, message = "CTA label must not exceed 120 characters")
            String ctaLabel,
            @Size(max = 512, message = "CTA URL must not exceed 512 characters")
            String ctaUrl
    ) {
    }

    public record InstructionResourceRequest(
            @NotBlank(message = "Resource type is required")
            @Size(max = 32, message = "Resource type must not exceed 32 characters")
            String type,
            @NotBlank(message = "Resource title is required")
            @Size(max = 180, message = "Resource title must not exceed 180 characters")
            String title,
            @NotBlank(message = "Resource description is required")
            @Size(max = 512, message = "Resource description must not exceed 512 characters")
            String description,
            @NotBlank(message = "Resource URL is required")
            @Size(max = 512, message = "Resource URL must not exceed 512 characters")
            String url
    ) {
    }
}
