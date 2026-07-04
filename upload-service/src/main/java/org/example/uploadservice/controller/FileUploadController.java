package org.example.uploadservice.controller;

import org.example.uploadservice.dto.ChunkPageDto;
import org.example.uploadservice.dto.EmbeddingStatusDto;
import org.example.uploadservice.dto.FileDetailsDto;
import org.example.uploadservice.entity.DocumentChunk;
import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.entity.UploadedFileProcessingLog;
import org.example.uploadservice.repository.DocumentChunkRepository;
import org.example.uploadservice.repository.UploadedFileProcessingLogRepository;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.example.uploadservice.service.*;
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
    private final EmbeddingStatusService embeddingStatusService;
    private final UploadedFileMaintenanceService uploadedFileMaintenanceService;
    private final ChunkQueryService chunkQueryService;
    private final FileDetailsService fileDetailsService;

    public FileUploadController(
            FileStorageService fileStorageService,
            UploadedFileRepository repository,
            DocumentChunkRepository chunkRepository,
            UploadedFileProcessingLogRepository logRepository,
            EmbeddingStatusService embeddingStatusService,
            UploadedFileMaintenanceService uploadedFileMaintenanceService,
            ChunkQueryService chunkQueryService,
            FileDetailsService fileDetailsService
    ) {
        this.fileStorageService = fileStorageService;
        this.repository = repository;
        this.chunkRepository = chunkRepository;
        this.logRepository = logRepository;
        this.embeddingStatusService = embeddingStatusService;
        this.uploadedFileMaintenanceService = uploadedFileMaintenanceService;
        this.chunkQueryService = chunkQueryService;
        this.fileDetailsService = fileDetailsService;
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
    public ResponseEntity<ChunkPageDto> getChunks(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(chunkQueryService.getChunks(fileId, page, size));
    }

    @GetMapping("/{fileId}/logs")
    public ResponseEntity<List<UploadedFileProcessingLog>> getLogs(@PathVariable Long fileId) {
        return ResponseEntity.ok(logRepository.findByFileIdOrderByCreatedAtAsc(fileId));
    }

    @GetMapping("/{fileId}/embedding-status")
    public ResponseEntity<EmbeddingStatusDto> getEmbeddingStatus(@PathVariable Long fileId) {
        return ResponseEntity.ok(embeddingStatusService.getEmbeddingStatus(fileId));
    }

    @PostMapping("/{fileId}/reprocess")
    public ResponseEntity<Void> reprocessFile(@PathVariable Long fileId) {
        uploadedFileMaintenanceService.reprocessFile(fileId);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        uploadedFileMaintenanceService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{fileId}/details")
    public ResponseEntity<FileDetailsDto> getFileDetails(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileDetailsService.getFileDetails(fileId));
    }
}