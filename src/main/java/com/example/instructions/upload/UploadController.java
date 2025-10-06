package com.example.instructions.upload;

import com.example.instructions.common.TooManyRequestsException;
import com.example.instructions.security.AuthenticationFacade;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Контроллер загрузки изображений.
 */
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final UploadService uploadService;
    private final UploadRateLimiter rateLimiter;
    private final AuthenticationFacade authenticationFacade;

    public UploadController(UploadService uploadService,
                            UploadRateLimiter rateLimiter,
                            AuthenticationFacade authenticationFacade) {
        this.uploadService = uploadService;
        this.rateLimiter = rateLimiter;
        this.authenticationFacade = authenticationFacade;
    }

    @PostMapping("/images")
    @PreAuthorize("hasRole('ADMIN')")
    public UploadService.UploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
        String userId = authenticationFacade.getCurrentUserId();
        if (!rateLimiter.tryAcquire(userId)) {
            throw new TooManyRequestsException("Превышен лимит загрузок изображений. Попробуйте позже");
        }
        return uploadService.storeImage(file);
    }
}
