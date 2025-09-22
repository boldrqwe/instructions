package com.boldin.instructions.service;

import com.boldin.instructions.domain.Instruction;
import com.boldin.instructions.domain.InstructionCategory;
import com.boldin.instructions.domain.InstructionCategoryRepository;
import com.boldin.instructions.domain.InstructionDifficulty;
import com.boldin.instructions.domain.InstructionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class InstructionServiceTest {

    @Autowired
    private InstructionRepository instructionRepository;

    @Autowired
    private InstructionCategoryRepository categoryRepository;

    private InstructionService service;

    private InstructionCategory category;

    @BeforeEach
    void setUp() {
        instructionRepository.deleteAll();
        categoryRepository.deleteAll();
        service = new InstructionService(instructionRepository, categoryRepository);
        category = categoryRepository.save(new InstructionCategory(
                "frontend",
                "Frontend",
                "Руководства по интерфейсам",
                "🧱"
        ));
    }

    @Test
    void createInstructionPersistsRichStructure() {
        InstructionDraft draft = new InstructionDraft(
                "html-basics",
                "HTML Basics",
                "Краткий обзор HTML",
                "Вводный текст",
                InstructionDifficulty.BEGINNER,
                25,
                "Желателен опыт работы с браузером",
                category.getSlug(),
                List.of("HTML", "Starter"),
                List.of(new InstructionSectionDraft(
                        "Каркас",
                        "Создаём базовый документ",
                        "Каркас",
                        "html",
                        "<h1>Hello</h1>",
                        "Попробовать",
                        "https://example.com"
                )),
                List.of(new InstructionResourceDraft(
                        "guide",
                        "MDN",
                        "Справочник",
                        "https://developer.mozilla.org"
                ))
        );

        Instruction created = service.create(draft);
        Instruction persisted = service.getById(created.getId());

        assertThat(persisted.getSlug()).isEqualTo("html-basics");
        assertThat(persisted.getDifficulty()).isEqualTo(InstructionDifficulty.BEGINNER);
        assertThat(persisted.getTags()).containsExactly("HTML", "Starter");
        assertThat(persisted.getSections()).hasSize(1);
        assertThat(persisted.getResources()).hasSize(1);
    }

    @Test
    void updateInstructionReplacesNestedCollections() {
        Instruction initial = service.create(new InstructionDraft(
                "html-basics",
                "HTML Basics",
                "Краткий обзор HTML",
                "Вводный текст",
                InstructionDifficulty.BEGINNER,
                25,
                null,
                category.getSlug(),
                List.of("HTML"),
                List.of(new InstructionSectionDraft(
                        "Каркас",
                        "Создаём базовый документ",
                        null,
                        null,
                        null,
                        null,
                        null
                )),
                List.of()
        ));

        InstructionDraft updateDraft = new InstructionDraft(
                "html-pro",
                "HTML Pro",
                "Расширенный HTML",
                "Новый вводный текст",
                InstructionDifficulty.INTERMEDIATE,
                40,
                "Опыт работы с семантикой",
                category.getSlug(),
                List.of("HTML", "Accessibility"),
                List.of(
                        new InstructionSectionDraft(
                                "Аудит",
                                "Проверяем доступность",
                                null,
                                null,
                                null,
                                "Открыть чеклист",
                                "https://frontendchecklist.io"
                        ),
                        new InstructionSectionDraft(
                                "Тесты",
                                "Добавляем линтер",
                                "Команда",
                                "bash",
                                "npm run lint",
                                null,
                                null
                        )
                ),
                List.of(new InstructionResourceDraft(
                        "cheatsheet",
                        "HTML Cheat Sheet",
                        "Сводка по тегам",
                        "https://htmlcheatsheet.com"
                ))
        );

        Instruction updated = service.update(initial.getId(), updateDraft);

        assertThat(updated.getSlug()).isEqualTo("html-pro");
        Instruction persisted = service.getById(initial.getId());
        assertThat(persisted.getSections()).hasSize(2);
        assertThat(persisted.getSections().get(0).getTitle()).isEqualTo("Аудит");
        assertThat(persisted.getResources()).singleElement()
                .satisfies(resource -> assertThat(resource.getType()).isEqualTo("cheatsheet"));
        assertThat(persisted.getTags()).containsExactly("HTML", "Accessibility");
        assertThat(persisted.getDifficulty()).isEqualTo(InstructionDifficulty.INTERMEDIATE);
    }

    @Test
    void getBySlugReturnsInstruction() {
        Instruction created = service.create(new InstructionDraft(
                "html-basics",
                "HTML Basics",
                "Краткий обзор HTML",
                "Вводный текст",
                InstructionDifficulty.BEGINNER,
                15,
                null,
                category.getSlug(),
                List.of(),
                List.of(new InstructionSectionDraft(
                        "Раздел",
                        "Описание",
                        null,
                        null,
                        null,
                        null,
                        null
                )),
                List.of()
        ));

        Instruction bySlug = service.getBySlug("html-basics");
        assertThat(bySlug.getId()).isEqualTo(created.getId());
    }

    @Test
    void getByIdThrowsIfMissing() {
        UUID missingId = UUID.randomUUID();
        assertThatThrownBy(() -> service.getById(missingId))
                .isInstanceOf(InstructionNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }
}
