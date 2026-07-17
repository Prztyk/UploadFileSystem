package org.example.uploadservice.dto;

public record AnswerTokenUsageDto(
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        Long totalDurationMs,
        Long promptEvalDurationMs,
        Long evalDurationMs
) {
}