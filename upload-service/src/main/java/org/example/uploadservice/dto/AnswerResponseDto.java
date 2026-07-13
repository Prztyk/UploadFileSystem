package org.example.uploadservice.dto;

import java.util.List;

public record AnswerResponseDto(
        String question,
        String answer,
        List<AnswerSourceDto> sources
) {
}