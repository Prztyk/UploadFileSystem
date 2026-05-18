package org.example.uploadservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final Path uploadDir = Paths.get("uploads");

    public FileUploadController() throws IOException {
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