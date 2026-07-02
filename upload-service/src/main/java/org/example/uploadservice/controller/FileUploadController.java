package org.example.uploadservice.controller;

import org.example.uploadservice.entity.DocumentChunk;
import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.entity.UploadedFileProcessingLog;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.example.uploadservice.repository.UploadedFileProcessingLogRepository;
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
    private final UploadedFileProcessingLogRepository logRepository;

    public FileUploadController(
            FileStorageService fileStorageService,
            UploadedFileRepository repository,
            DocumentChunkRepository chunkRepository,
            UploadedFileProcessingLogRepository logRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.repository = repository;
        this.chunkRepository = chunkRepository;
        this.logRepository = logRepository;
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

    @GetMapping("/{fileId}/logs")
    public ResponseEntity<List<UploadedFileProcessingLog>> getLogs(@PathVariable Long fileId) {
        return ResponseEntity.ok(logRepository.findByFileIdOrderByCreatedAtAsc(fileId));
    }
}