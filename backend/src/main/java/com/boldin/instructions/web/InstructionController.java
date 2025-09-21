package com.boldin.instructions.web;

import com.boldin.instructions.domain.Instruction;
import com.boldin.instructions.service.InstructionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instructions")
@CrossOrigin
public class InstructionController {

    private final InstructionService instructionService;

    public InstructionController(InstructionService instructionService) {
        this.instructionService = instructionService;
    }

    @GetMapping
    public List<InstructionResponse> getAll() {
        return instructionService.findAll().stream()
                .map(InstructionResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public InstructionResponse getById(@PathVariable UUID id) {
        Instruction instruction = instructionService.getById(id);
        return InstructionResponse.fromEntity(instruction);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructionResponse create(@Valid @RequestBody InstructionRequest request) {
        Instruction created = instructionService.create(request.title(), request.content());
        return InstructionResponse.fromEntity(created);
    }

    @PutMapping("/{id}")
    public InstructionResponse update(@PathVariable UUID id, @Valid @RequestBody InstructionRequest request) {
        Instruction updated = instructionService.update(id, request.title(), request.content());
        return InstructionResponse.fromEntity(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        instructionService.delete(id);
    }
}
