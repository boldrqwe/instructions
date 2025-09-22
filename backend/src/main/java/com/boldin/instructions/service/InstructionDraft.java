package com.boldin.instructions.service;

import com.boldin.instructions.domain.InstructionDifficulty;

import java.util.List;

public record InstructionDraft(
        String slug,
        String title,
        String summary,
        String introduction,
        InstructionDifficulty difficulty,
        int estimatedMinutes,
        String prerequisites,
        String categorySlug,
        List<String> tags,
        List<InstructionSectionDraft> sections,
        List<InstructionResourceDraft> resources
) {
}
