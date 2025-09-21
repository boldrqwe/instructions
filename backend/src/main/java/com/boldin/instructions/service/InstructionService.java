package com.boldin.instructions.service;

import com.boldin.instructions.domain.Instruction;
import com.boldin.instructions.domain.InstructionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InstructionService {

    private final InstructionRepository repository;

    public InstructionService(InstructionRepository repository) {
        this.repository = repository;
    }

    public Instruction create(String title, String content) {
        Instruction instruction = new Instruction(title, content);
        return repository.save(instruction);
    }

    public Instruction update(UUID id, String title, String content) {
        Instruction instruction = getById(id);
        instruction.setTitle(title);
        instruction.setContent(content);
        return repository.save(instruction);
    }

    public void delete(UUID id) {
        Instruction instruction = getById(id);
        repository.delete(instruction);
    }

    @Transactional(readOnly = true)
    public Instruction getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new InstructionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Instruction> findAll() {
        return repository.findAll();
    }
}
