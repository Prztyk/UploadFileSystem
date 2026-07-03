package org.example.uploadservice.dto;

public record EmbeddingStatusDto(
        Long fileId,
        String modelName,
        Long chunkCount,
        Long embeddingCount,
        Long missingEmbeddingCount,
        Boolean fullyEmbedded
) {
}