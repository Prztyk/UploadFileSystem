package org.example.uploadservice.service;

import org.example.uploadservice.dto.EmbeddingStatusDto;
import org.example.uploadservice.dto.FileDetailsDto;
import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.entity.UploadedFileProcessingLog;
import org.example.uploadservice.repository.UploadedFileProcessingLogRepository;
import org.example.uploadservice.repository.UploadedFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileDetailsService {

    private final UploadedFileRepository fileRepository;
    private final UploadedFileProcessingLogRepository logRepository;
    private final EmbeddingStatusService embeddingStatusService;

    public FileDetailsService(
            UploadedFileRepository fileRepository,
            UploadedFileProcessingLogRepository logRepository,
            EmbeddingStatusService embeddingStatusService
    ) {
        this.fileRepository = fileRepository;
        this.logRepository = logRepository;
        this.embeddingStatusService = embeddingStatusService;
    }

    public FileDetailsDto getFileDetails(Long fileId) {
        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found: " + fileId));

        EmbeddingStatusDto embeddingStatus = embeddingStatusService.getEmbeddingStatus(fileId);

        List<UploadedFileProcessingLog> logs =
                logRepository.findByFileIdOrderByCreatedAtAsc(fileId);

        return new FileDetailsDto(
                file,
                embeddingStatus,
                logs
        );
    }
}