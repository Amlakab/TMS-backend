package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadImages(@RequestParam("images") MultipartFile[] images) throws IOException {
        List<String> filePaths = new ArrayList<>();
        for (MultipartFile file : images) {
            String filename = fileStorageService.storeFile(file);
            filePaths.add("/uploads/" + filename); // e.g., /uploads/abc.png
        }

        return ResponseEntity.ok(filePaths);
    }
}