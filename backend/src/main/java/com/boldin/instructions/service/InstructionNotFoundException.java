package com.boldin.instructions.service;

import java.util.UUID;

public class InstructionNotFoundException extends RuntimeException {

    public InstructionNotFoundException(UUID id) {
        super("Instruction with id " + id + " not found");
    }
}
