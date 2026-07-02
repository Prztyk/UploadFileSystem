package org.example.uploadservice.dto;

public record SemanticSearchResultDto(
        Long chunkId,
        Long fileId,
        Integer chunkIndex,
        String originalFilename,
        String previousContent,
        String content,
        String nextContent,
        Double distance,
        Double similarityScore,
        Double lexicalScore,
        Boolean exactPhraseMatch,
        Double hybridScore
) {
}