package org.example.uploadservice.service;

import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
public class FileStorageService {

    private final UploadedFileRepository repository;
    private final DocumentProcessingService processingService;
    private final Path uploadDir = Paths.get("uploads");

    public FileStorageService(
            UploadedFileRepository repository,
            DocumentProcessingService processingService
    ) throws IOException {
        this.repository = repository;
        this.processingService = processingService;

        Files.createDirectories(uploadDir);
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Nie wybrano pliku.");
        }

        String originalName = file.getOriginalFilename();

        String safeFileName = Path.of(originalName == null ? "file" : originalName)
                .getFileName()
                .toString();

        Path targetPath = uploadDir.resolve(safeFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOriginalFilename(originalName);
        uploadedFile.setStoredFilename(safeFileName);
        uploadedFile.setContentType(file.getContentType());
        uploadedFile.setSize(file.getSize());
        uploadedFile.setStatus("UPLOADED");
        uploadedFile.setCreatedAt(LocalDateTime.now());

        repository.save(uploadedFile);

        processingService.processFile(uploadedFile);

        return "Plik zapisany: " + safeFileName;
    }
}