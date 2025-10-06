package com.example.instructions.upload;

import com.example.instructions.common.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис загрузки файлов.
 */
@Service
public class UploadService {

    private static final Path BASE_PATH = Paths.get("uploads", "images");

    public UploadResponse storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл не может быть пустым");
        }
        LocalDate today = LocalDate.now();
        Path targetDirectory = BASE_PATH
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .resolve(String.format("%02d", today.getDayOfMonth()));
        try {
            Files.createDirectories(targetDirectory);
            String extension = determineExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + extension;
            Path targetFile = targetDirectory.resolve(fileName);
            file.transferTo(targetFile);
            String url = "/uploads/images/" + today.getYear() + "/"
                    + String.format("%02d", today.getMonthValue()) + "/"
                    + String.format("%02d", today.getDayOfMonth()) + "/" + fileName;
            return new UploadResponse(url);
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось сохранить файл", ex);
        }
    }

    private String determineExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return extension != null && !extension.isBlank() ? "." + extension.toLowerCase() : "";
    }

    /**
     * Ответ при загрузке.
     */
    public record UploadResponse(String url) {
    }
}
