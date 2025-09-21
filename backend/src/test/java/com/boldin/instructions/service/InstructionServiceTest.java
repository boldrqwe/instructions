package com.boldin.instructions.service;

import com.boldin.instructions.domain.Instruction;
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
    private InstructionRepository repository;

    private InstructionService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        service = new InstructionService(repository);
    }

    @Test
    void createInstructionPersistsEntity() {
        Instruction instruction = service.create("Тест", "Проверка сохранения");

        List<Instruction> all = repository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getTitle()).isEqualTo("Тест");
        assertThat(instruction.getId()).isNotNull();
    }

    @Test
    void updateInstructionChangesFields() {
        Instruction instruction = service.create("Старое", "Описание");

        Instruction updated = service.update(instruction.getId(), "Новое", "Обновленное описание");

        assertThat(updated.getTitle()).isEqualTo("Новое");
        assertThat(updated.getContent()).isEqualTo("Обновленное описание");
        assertThat(repository.findById(instruction.getId())).contains(updated);
    }

    @Test
    void deleteInstructionRemovesEntity() {
        Instruction instruction = service.create("Удалить", "Удаляем");

        service.delete(instruction.getId());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void getByIdThrowsIfMissing() {
        UUID missingId = UUID.randomUUID();
        assertThatThrownBy(() -> service.getById(missingId))
                .isInstanceOf(InstructionNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }
}
