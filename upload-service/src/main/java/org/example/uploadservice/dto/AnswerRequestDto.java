package org.example.uploadservice.dto;

public record AnswerRequestDto(
        String question,
        Integer limit,
        Double minSimilarity
) {
}