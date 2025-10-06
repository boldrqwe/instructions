package com.example.instructions.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.instructions.security.AuthenticationFacade;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

/**
 * WebMvc тесты UploadController.
 */
@WebMvcTest(controllers = UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadService uploadService;

    @MockBean
    private UploadRateLimiter uploadRateLimiter;

    @MockBean
    private AuthenticationFacade authenticationFacade;

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
    void uploadImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        when(authenticationFacade.getCurrentUserId()).thenReturn("admin");
        when(uploadRateLimiter.tryAcquire("admin")).thenReturn(true);
        when(uploadService.storeImage(any(MultipartFile.class))).thenReturn(new UploadService.UploadResponse("/uploads/images/x.png"));

        mockMvc.perform(multipart("/api/v1/uploads/images").file(file)
                        .with(adminRequest()))
                .andExpect(status().isOk());
    }

    @Test
    void uploadImage_rateLimited() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1});
        when(authenticationFacade.getCurrentUserId()).thenReturn("admin");
        when(uploadRateLimiter.tryAcquire("admin")).thenReturn(false);

        mockMvc.perform(multipart("/api/v1/uploads/images").file(file)
                        .with(adminRequest()))
                .andExpect(status().isTooManyRequests());
    }
}
