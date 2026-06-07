package org.example.uploadservice.controller;

import org.example.uploadservice.entity.DocumentChunk;
import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.example.uploadservice.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UploadedFileRepository repository;
    private final DocumentChunkRepository chunkRepository;

    public FileUploadController(
            FileStorageService fileStorageService,
            UploadedFileRepository repository,
            DocumentChunkRepository chunkRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.repository = repository;
        this.chunkRepository = chunkRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String result = fileStorageService.store(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<UploadedFile>> getUploadHistory() {
        List<UploadedFile> files = repository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}/chunks")
    public ResponseEntity<List<DocumentChunk>> getChunks(@PathVariable Long fileId) {
        List<DocumentChunk> chunks = chunkRepository.findByFileIdOrderByChunkIndexAsc(fileId);
        return ResponseEntity.ok(chunks);
    }
}