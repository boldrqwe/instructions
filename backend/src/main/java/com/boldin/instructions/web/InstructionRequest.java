package com.boldin.instructions.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstructionRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 180, message = "Title must not exceed 180 characters")
        String title,
        @NotBlank(message = "Content is required")
        @Size(max = 4096, message = "Content must not exceed 4096 characters")
        String content
) {
}
