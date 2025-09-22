package com.boldin.instructions.service;

import com.boldin.instructions.domain.Instruction;
import com.boldin.instructions.domain.InstructionCategory;
import com.boldin.instructions.domain.InstructionCategoryRepository;
import com.boldin.instructions.domain.InstructionRepository;
import com.boldin.instructions.domain.InstructionResource;
import com.boldin.instructions.domain.InstructionSection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InstructionService {

    private final InstructionRepository repository;
    private final InstructionCategoryRepository categoryRepository;

    public InstructionService(InstructionRepository repository,
                              InstructionCategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    public Instruction create(InstructionDraft draft) {
        InstructionCategory category = getCategory(draft.categorySlug());
        Instruction instruction = new Instruction();
        applyDraft(instruction, category, draft);
        return repository.save(instruction);
    }

    public Instruction update(UUID id, InstructionDraft draft) {
        Instruction instruction = getById(id);
        InstructionCategory category = getCategory(draft.categorySlug());
        applyDraft(instruction, category, draft);
        return repository.save(instruction);
    }

    public void delete(UUID id) {
        Instruction instruction = getById(id);
        repository.delete(instruction);
    }

    @Transactional(readOnly = true)
    public Instruction getById(UUID id) {
        return repository.findByIdDetailed(id)
                .orElseThrow(() -> new InstructionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Instruction getBySlug(String slug) {
        return repository.findBySlugDetailed(slug)
                .orElseThrow(() -> new InstructionNotFoundException(slug));
    }

    @Transactional(readOnly = true)
    public List<Instruction> findAll() {
        return repository.findAllDetailed();
    }

    private InstructionCategory getCategory(String slug) {
        return categoryRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new InstructionCategoryNotFoundException(slug));
    }

    private void applyDraft(Instruction instruction, InstructionCategory category, InstructionDraft draft) {
        instruction.setCategory(category);
        instruction.setSlug(draft.slug());
        instruction.setTitle(draft.title());
        instruction.setSummary(draft.summary());
        instruction.setIntroduction(draft.introduction());
        instruction.setDifficulty(draft.difficulty());
        instruction.setEstimatedMinutes(draft.estimatedMinutes());
        instruction.setPrerequisites(draft.prerequisites());
        instruction.setTags(draft.tags());
        instruction.setSections(buildSections(instruction, draft.sections()));
        instruction.setResources(buildResources(instruction, draft.resources()));
    }

    private List<InstructionSection> buildSections(Instruction instruction, List<InstructionSectionDraft> drafts) {
        List<InstructionSection> sections = new ArrayList<>();
        if (drafts == null) {
            return sections;
        }
        int position = 1;
        for (InstructionSectionDraft draft : drafts) {
            InstructionSection section = new InstructionSection();
            section.setInstruction(instruction);
            section.setPosition(position++);
            section.setTitle(draft.title());
            section.setContent(draft.content());
            section.setCodeTitle(draft.codeTitle());
            section.setCodeLanguage(draft.codeLanguage());
            section.setCodeSnippet(draft.codeSnippet());
            section.setCtaLabel(draft.ctaLabel());
            section.setCtaUrl(draft.ctaUrl());
            sections.add(section);
        }
        return sections;
    }

    private List<InstructionResource> buildResources(Instruction instruction, List<InstructionResourceDraft> drafts) {
        List<InstructionResource> resources = new ArrayList<>();
        if (drafts == null) {
            return resources;
        }
        int position = 1;
        for (InstructionResourceDraft draft : drafts) {
            InstructionResource resource = new InstructionResource();
            resource.setInstruction(instruction);
            resource.setPosition(position++);
            resource.setType(draft.type());
            resource.setTitle(draft.title());
            resource.setDescription(draft.description());
            resource.setUrl(draft.url());
            resources.add(resource);
        }
        return resources;
    }
}
