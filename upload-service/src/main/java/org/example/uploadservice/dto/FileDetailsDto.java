package org.example.uploadservice.dto;

import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.entity.UploadedFileProcessingLog;

import java.util.List;

public record FileDetailsDto(
        UploadedFile file,
        EmbeddingStatusDto embeddingStatus,
        List<UploadedFileProcessingLog> logs
) {
}