package com.boldin.instructions.web;

import com.boldin.instructions.domain.Instruction;
import com.boldin.instructions.domain.InstructionDifficulty;
import com.boldin.instructions.service.InstructionDraft;
import com.boldin.instructions.service.InstructionResourceDraft;
import com.boldin.instructions.service.InstructionSectionDraft;
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
    public List<InstructionSummaryResponse> getAll() {
        return instructionService.findAll().stream()
                .map(InstructionSummaryResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public InstructionResponse getById(@PathVariable UUID id) {
        Instruction instruction = instructionService.getById(id);
        return InstructionResponse.fromEntity(instruction);
    }

    @GetMapping("/slug/{slug}")
    public InstructionResponse getBySlug(@PathVariable String slug) {
        Instruction instruction = instructionService.getBySlug(slug);
        return InstructionResponse.fromEntity(instruction);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructionResponse create(@Valid @RequestBody InstructionRequest request) {
        InstructionDraft draft = toDraft(request);
        Instruction created = instructionService.create(draft);
        return InstructionResponse.fromEntity(created);
    }

    @PutMapping("/{id}")
    public InstructionResponse update(@PathVariable UUID id, @Valid @RequestBody InstructionRequest request) {
        InstructionDraft draft = toDraft(request);
        Instruction updated = instructionService.update(id, draft);
        return InstructionResponse.fromEntity(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        instructionService.delete(id);
    }

    private InstructionDraft toDraft(InstructionRequest request) {
        List<String> tags = request.tags() == null ? List.of() : List.copyOf(request.tags());
        List<InstructionSectionDraft> sections = request.sections() == null ? List.of() : request.sections().stream()
                .map(section -> new InstructionSectionDraft(
                        section.title(),
                        section.content(),
                        section.codeTitle(),
                        section.codeLanguage(),
                        section.codeSnippet(),
                        section.ctaLabel(),
                        section.ctaUrl()
                ))
                .toList();
        List<InstructionResourceDraft> resources = request.resources() == null ? List.of() : request.resources().stream()
                .map(resource -> new InstructionResourceDraft(
                        resource.type(),
                        resource.title(),
                        resource.description(),
                        resource.url()
                ))
                .toList();

        return new InstructionDraft(
                request.slug(),
                request.title(),
                request.summary(),
                request.introduction(),
                InstructionDifficulty.fromValue(request.difficulty()),
                request.estimatedMinutes(),
                request.prerequisites(),
                request.categorySlug(),
                tags,
                sections,
                resources
        );
    }
}
