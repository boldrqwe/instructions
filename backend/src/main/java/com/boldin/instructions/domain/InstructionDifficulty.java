package com.boldin.instructions.domain;

import java.util.Arrays;

public enum InstructionDifficulty {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String value;

    InstructionDifficulty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static InstructionDifficulty fromValue(String value) {
        return Arrays.stream(values())
                .filter(difficulty -> difficulty.value.equalsIgnoreCase(value)
                        || difficulty.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown instruction difficulty: " + value));
    }
}
