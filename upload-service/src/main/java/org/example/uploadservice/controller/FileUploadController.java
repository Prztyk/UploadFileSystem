package org.example.uploadservice.controller;

import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final UploadedFileRepository repository;
    private final Path uploadDir = Paths.get("uploads");

    public FileUploadController(UploadedFileRepository repository) throws IOException {
        this.repository = repository;
        Files.createDirectories(uploadDir);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Nie wybrano pliku.");
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

        return ResponseEntity.ok("Plik zapisany przez upload-service: " + safeFileName);
    }

    @GetMapping
    public ResponseEntity<List<String>> listFiles() throws IOException {
        try (Stream<Path> paths = Files.list(uploadDir)) {
            List<String> files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList();

            return ResponseEntity.ok(files);
        }
    }
}