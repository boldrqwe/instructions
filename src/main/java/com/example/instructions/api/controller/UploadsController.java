package com.example.instructions.api.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Контроллер загрузки изображений.
 */
@RestController
@RequestMapping("/api/v1/uploads")
@PreAuthorize("hasRole('ADMIN')")
public class UploadsController {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            MediaType.IMAGE_PNG_VALUE, ".png",
            MediaType.IMAGE_JPEG_VALUE, ".jpg",
            "image/webp", ".webp",
            MediaType.IMAGE_GIF_VALUE, ".gif"
    );

    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не найден");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Файл слишком большой");
        }
        String contentType = file.getContentType();
        String extension = ALLOWED_TYPES.get(contentType);
        if (!StringUtils.hasText(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неподдерживаемый тип файла");
        }
        LocalDate now = LocalDate.now();
        Path directory = Paths.get("uploads", "images",
                String.format("%04d", now.getYear()),
                String.format("%02d", now.getMonthValue()),
                String.format("%02d", now.getDayOfMonth()));
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(UUID.randomUUID() + extension);
            file.transferTo(target);
            String relativePath = directory.resolve(target.getFileName()).toString().replace('\\', '/');
            return new UploadResponse("/" + relativePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить файл", ex);
        }
    }

    /**
     * Ответ с URL.
     */
    public record UploadResponse(@JsonProperty("url") String url) {
    }
}
