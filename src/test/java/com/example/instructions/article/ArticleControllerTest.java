//package com.example.instructions.article;
//
//import com.example.instructions.article.dto.ArticleRequest;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class ArticleControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private ArticleRepository articleRepository;
//
//    @BeforeEach
//    void setUp() {
//        articleRepository.deleteAll();
//    }
//
//    @Test
//    void shouldCreateAndReturnArticle() throws Exception {
//        ArticleRequest request = new ArticleRequest("Новая инструкция", "<p>Содержимое</p>");
//
//        MvcResult result = mockMvc.perform(post("/api/articles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").isNumber())
//                .andExpect(jsonPath("$.title").value("Новая инструкция"))
//                .andReturn();
//
//        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
//        long articleId = responseJson.get("id").asLong();
//
//        mockMvc.perform(get("/api/articles/{id}", articleId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("Новая инструкция"))
//                .andExpect(jsonPath("$.content").value("<p>Содержимое</p>"));
//
//        mockMvc.perform(get("/api/articles"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(articleId));
//    }
//
//    @Test
//    void shouldValidateInput() throws Exception {
//        ArticleRequest invalidRequest = new ArticleRequest("", "");
//
//        mockMvc.perform(post("/api/articles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Данные заполнены некорректно"));
//    }
//
//    @Test
//    void shouldUpdateExistingArticle() throws Exception {
//        Article saved = articleRepository.save(new Article("Черновик", "<p>Черновик</p>"));
//        ArticleRequest request = new ArticleRequest("Обновлённый заголовок", "<p>Обновлённое содержимое</p>");
//
//        mockMvc.perform(put("/api/articles/{id}", saved.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("Обновлённый заголовок"));
//
//        Article updated = articleRepository.findById(saved.getId()).orElseThrow();
//        assertThat(updated.getTitle()).isEqualTo("Обновлённый заголовок");
//        assertThat(updated.getContent()).isEqualTo("<p>Обновлённое содержимое</p>");
//    }
//
//    @Test
//    void shouldDeleteArticle() throws Exception {
//        Article saved = articleRepository.save(new Article("Удаляемая", "<p>Текст</p>"));
//
//        mockMvc.perform(delete("/api/articles/{id}", saved.getId()))
//                .andExpect(status().isNoContent());
//
//        assertThat(articleRepository.existsById(saved.getId())).isFalse();
//    }
//
//    @Test
//    void shouldReturnNotFoundForMissingArticle() throws Exception {
//        mockMvc.perform(get("/api/articles/{id}", 9999))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Статья с идентификатором 9999 не найдена"));
//    }
//}
