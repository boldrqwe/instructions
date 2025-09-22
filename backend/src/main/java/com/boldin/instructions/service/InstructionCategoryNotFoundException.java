package com.boldin.instructions.service;

public class InstructionCategoryNotFoundException extends RuntimeException {

    public InstructionCategoryNotFoundException(String slug) {
        super("Instruction category with slug '" + slug + "' not found");
    }
}
