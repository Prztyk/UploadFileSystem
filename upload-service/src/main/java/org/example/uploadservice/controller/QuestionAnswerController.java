package org.example.uploadservice.controller;

import org.example.uploadservice.dto.AnswerRequestDto;
import org.example.uploadservice.dto.AnswerResponseDto;
import org.example.uploadservice.service.QuestionAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
public class QuestionAnswerController {

    private final QuestionAnswerService questionAnswerService;

    public QuestionAnswerController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    @PostMapping
    public ResponseEntity<AnswerResponseDto> answerQuestion(
            @RequestBody AnswerRequestDto request
    ) {
        return ResponseEntity.ok(questionAnswerService.answerQuestion(request));
    }
}