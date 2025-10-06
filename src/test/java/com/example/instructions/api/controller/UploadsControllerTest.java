package com.example.instructions.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

@WebMvcTest(controllers = UploadsController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanUploads() throws IOException {
        Path uploads = Path.of("uploads");
        if (Files.exists(uploads)) {
            FileSystemUtils.deleteRecursively(uploads);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadImageSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/uploads/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(Matchers.startsWith("/uploads/images/")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadImageTooLarge() throws Exception {
        byte[] bytes = new byte[(int) (10 * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, bytes);

        mockMvc.perform(multipart("/api/v1/uploads/images").file(file))
                .andExpect(status().isPayloadTooLarge());
    }
}
