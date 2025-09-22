package com.boldin.instructions.service;

public record InstructionResourceDraft(
        String type,
        String title,
        String description,
        String url
) {
}
