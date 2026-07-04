package org.example.uploadservice.dto;

import org.example.uploadservice.entity.DocumentChunk;

import java.util.List;

public record ChunkPageDto(
        Long fileId,
        List<DocumentChunk> chunks,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
) {
}