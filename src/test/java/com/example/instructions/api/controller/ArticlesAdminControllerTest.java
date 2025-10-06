package com.example.instructions.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.instructions.api.article.dto.ArticleCreateDto;
import com.example.instructions.api.article.dto.ArticleResponseDto;
import com.example.instructions.api.article.dto.ArticleUpdateDto;
import com.example.instructions.common.PageResponse;
import com.example.instructions.domain.ArticleStatus;
import com.example.instructions.service.ArticleEditorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ArticlesAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArticlesAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleEditorService articleEditorService;

    private ArticleResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new ArticleResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setTitle("Test Article");
        responseDto.setSlug("test-article");
        responseDto.setSummary("Summary");
        responseDto.setTags(List.of("tag1", "tag2"));
        responseDto.setCoverImageUrl("/cover.png");
        responseDto.setContentHtml("<p>Content</p>");
        responseDto.setContentJson(JsonNodeFactory.instance.objectNode());
        responseDto.setStatus(ArticleStatus.DRAFT);
        responseDto.setCreatedAt(OffsetDateTime.now());
        responseDto.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArticle() throws Exception {
        ArticleCreateDto createDto = new ArticleCreateDto();
        createDto.setTitle("New Article");
        createDto.setSlug("new-article");
        when(articleEditorService.create(any(ArticleCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("test-article"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateArticle() throws Exception {
        ArticleUpdateDto updateDto = new ArticleUpdateDto();
        updateDto.setTitle("Updated");
        when(articleEditorService.update(eq(responseDto.getId()), any(ArticleUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/articles/" + responseDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void publishArticle() throws Exception {
        when(articleEditorService.publish(responseDto.getId())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/articles/" + responseDto.getId() + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unpublishArticle() throws Exception {
        when(articleEditorService.unpublish(responseDto.getId())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/articles/" + responseDto.getId() + "/unpublish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listArticles() throws Exception {
        PageResponse<ArticleResponseDto> page = new PageResponse<>(List.of(responseDto), 0, 20, 1);
        when(articleEditorService.list(null, null, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("test-article"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getArticle() throws Exception {
        when(articleEditorService.get(responseDto.getId())).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/articles/" + responseDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()));
    }
}
