package org.example.uploadservice.dto;

public record SemanticSearchResultDto(
        Long chunkId,
        Long fileId,
        Integer chunkIndex,
        String originalFilename,
        String content,
        Double distance
) {
}