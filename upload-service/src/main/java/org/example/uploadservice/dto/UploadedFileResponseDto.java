package org.example.uploadservice.dto;

import org.example.uploadservice.entity.UploadedFile;
import org.example.uploadservice.enums.UploadedFileStatus;

import java.time.LocalDateTime;

public record UploadedFileResponseDto(
        Long id,
        String originalFilename,
        String storedFilename,
        String contentType,
        Long size,
        UploadedFileStatus status,
        LocalDateTime createdAt
) {

    public static UploadedFileResponseDto from(UploadedFile uploadedFile) {
        return new UploadedFileResponseDto(
                uploadedFile.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStoredFilename(),
                uploadedFile.getContentType(),
                uploadedFile.getSize(),
                uploadedFile.getStatus(),
                uploadedFile.getCreatedAt()
        );
    }
}