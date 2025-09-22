package com.boldin.instructions.service;

public record InstructionSectionDraft(
        String title,
        String content,
        String codeTitle,
        String codeLanguage,
        String codeSnippet,
        String ctaLabel,
        String ctaUrl
) {
}
