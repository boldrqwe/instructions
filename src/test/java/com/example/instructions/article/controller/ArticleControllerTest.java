package com.example.instructions.article.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.instructions.article.dto.ArticleCreateDto;
import com.example.instructions.article.dto.ArticleResponseDto;
import com.example.instructions.article.dto.ArticleUpdateDto;
import com.example.instructions.article.model.ArticleStatus;
import com.example.instructions.article.service.ArticleService;
import com.example.instructions.common.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * WebMvc тесты ArticleController.
 */
@WebMvcTest(controllers = ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleService articleService;

    private ArticleResponseDto articleResponse;

    @BeforeEach
    void setUp() {
        articleResponse = new ArticleResponseDto();
        articleResponse.setId(UUID.randomUUID());
        articleResponse.setTitle("Title");
        articleResponse.setSlug("title");
        articleResponse.setStatus(ArticleStatus.DRAFT);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor adminRequest() {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("admin", "pass",
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            return request;
        };
    }

    @Test
    void createArticle_shouldReturnResponse() throws Exception {
        when(articleService.createArticle(any(ArticleCreateDto.class))).thenReturn(articleResponse);

        ArticleCreateDto request = new ArticleCreateDto();
        request.setTitle("Title");
        request.setContentJson(objectMapper.createObjectNode());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/articles")
                        .with(adminRequest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.slug").value("title"));

        verify(articleService).createArticle(any(ArticleCreateDto.class));
    }

    @Test
    void updateArticle_shouldReturnResponse() throws Exception {
        when(articleService.updateArticle(any(UUID.class), any(ArticleUpdateDto.class))).thenReturn(articleResponse);

        ArticleUpdateDto request = new ArticleUpdateDto();
        request.setTitle("New Title");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/articles/{id}", UUID.randomUUID())
                        .with(adminRequest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.slug").value("title"));

        verify(articleService).updateArticle(any(UUID.class), any(ArticleUpdateDto.class));
    }

    @Test
    void publishArticle_shouldCallService() throws Exception {
        when(articleService.publishArticle(any(UUID.class))).thenReturn(articleResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/articles/{id}/publish", UUID.randomUUID())
                        .with(adminRequest()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).publishArticle(any(UUID.class));
    }

    @Test
    void unpublishArticle_shouldCallService() throws Exception {
        when(articleService.unpublishArticle(any(UUID.class))).thenReturn(articleResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/articles/{id}/unpublish", UUID.randomUUID())
                        .with(adminRequest()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).unpublishArticle(any(UUID.class));
    }

    @Test
    void listArticles_asAdmin_passesStatusFromRequest() throws Exception {
        when(articleService.searchArticles(any(ArticleStatus.class), anyString(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(new PageResponse<>(List.of(articleResponse), 0, 20, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/articles")
                        .with(adminRequest())
                        .param("status", "DRAFT")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10")
                        .param("authorId", "admin"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).searchArticles(ArticleStatus.DRAFT, "test", 0, 10, "admin", true);
    }

    @Test
    void listArticles_public_forcesPublished() throws Exception {
        when(articleService.searchArticles(any(ArticleStatus.class), anyString(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(new PageResponse<>(List.of(articleResponse), 0, 20, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/articles")
                        .param("status", "DRAFT"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).searchArticles(ArticleStatus.PUBLISHED, null, 0, 20, null, false);
    }

    @Test
    void getArticle_asAdmin() throws Exception {
        when(articleService.getArticle(any(UUID.class))).thenReturn(articleResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/articles/{id}", UUID.randomUUID())
                        .with(adminRequest()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).getArticle(any(UUID.class));
    }

    @Test
    void getBySlug_public() throws Exception {
        when(articleService.getPublishedBySlug("title")).thenReturn(articleResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/articles/by-slug/{slug}", "title"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.slug").value("title"));

        verify(articleService).getPublishedBySlug("title");
    }
}
