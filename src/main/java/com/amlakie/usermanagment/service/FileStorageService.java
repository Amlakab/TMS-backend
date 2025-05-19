package com.amlakie.usermanagment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${server.base-url}")
    private String serverBaseUrl;

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        return getFileUrl(uniqueFilename);
    }

    public byte[] loadFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String filename) throws IOException {
        String actualFilename = extractFilenameFromUrl(filename);
        Path filePath = Paths.get(uploadDir).resolve(actualFilename);
        Files.deleteIfExists(filePath);
    }

    public String getFileUrl(String filename) {
        return serverBaseUrl + "/uploads/" + filename;
    }

    private String extractFilenameFromUrl(String url) {
        if (url.startsWith(serverBaseUrl)) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return url;
    }
}