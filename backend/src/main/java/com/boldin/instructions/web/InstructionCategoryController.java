package com.boldin.instructions.web;

import com.boldin.instructions.domain.InstructionCategory;
import com.boldin.instructions.domain.InstructionCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instruction-categories")
@CrossOrigin
public class InstructionCategoryController {

    private final InstructionCategoryRepository repository;

    public InstructionCategoryController(InstructionCategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<InstructionCategoryResponse> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "title")).stream()
                .map(InstructionCategoryController::toResponse)
                .toList();
    }

    private static InstructionCategoryResponse toResponse(InstructionCategory category) {
        return new InstructionCategoryResponse(
                category.getId(),
                category.getSlug(),
                category.getTitle(),
                category.getDescription(),
                category.getIcon()
        );
    }

    public record InstructionCategoryResponse(
            java.util.UUID id,
            String slug,
            String title,
            String description,
            String icon
    ) {
    }
}
