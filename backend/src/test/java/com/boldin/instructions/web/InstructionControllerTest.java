package com.boldin.instructions.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InstructionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllReturnsStructuredSummaries() throws Exception {
        mockMvc.perform(get("/api/instructions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].slug", Matchers.hasItems(
                        "html-building-blocks",
                        "spring-boot-rest-blueprint",
                        "dockerized-local-environment"
                )))
                .andExpect(jsonPath("$[?(@.slug=='html-building-blocks')].difficulty")
                        .value(Matchers.contains("beginner")))
                .andExpect(jsonPath("$[?(@.slug=='spring-boot-rest-blueprint')].tags[0]")
                        .value(Matchers.contains("Spring Boot")));
    }

    @Test
    void createInstructionReturnsCreatedResource() throws Exception {
        InstructionRequest request = new InstructionRequest(
                "test-guide",
                "Тестовый гайд",
                "Как за 20 минут подготовить гайд",
                "Набор шагов для быстрого запуска",
                "beginner",
                20,
                "frontend-fundamentals",
                "Опыт чтения документации",
                List.of("Testing", "Guides"),
                List.of(new InstructionRequest.InstructionSectionRequest(
                        "Собираем каркас",
                        "Определяем цель и аудиторию",
                        "Список вопросов",
                        "markdown",
                        "- Кто читатель?\n- Какие у него задачи?",
                        "Скачать шаблон",
                        "https://example.com/template"
                )),
                List.of(new InstructionRequest.InstructionResourceRequest(
                        "guide",
                        "Style guide",
                        "Набор правил по оформлению",
                        "https://example.com/style"
                ))
        );

        mockMvc.perform(post("/api/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("test-guide"))
                .andExpect(jsonPath("$.difficulty").value("beginner"))
                .andExpect(jsonPath("$.category.slug").value("frontend-fundamentals"))
                .andExpect(jsonPath("$.sections", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.resources", Matchers.hasSize(1)));
    }
}
