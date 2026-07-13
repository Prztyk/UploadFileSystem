package org.example.uploadservice.dto;

public record AnswerSourceDto(
        Integer sourceNumber,
        Long chunkId,
        Long fileId,
        Integer chunkIndex,
        String originalFilename,
        String searchMode,
        String matchType,
        Double hybridScore
) {
}