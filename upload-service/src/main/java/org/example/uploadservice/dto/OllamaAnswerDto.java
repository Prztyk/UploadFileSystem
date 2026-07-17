package org.example.uploadservice.dto;

public record OllamaAnswerDto(
        String answer,
        AnswerTokenUsageDto tokenUsage
) {
}