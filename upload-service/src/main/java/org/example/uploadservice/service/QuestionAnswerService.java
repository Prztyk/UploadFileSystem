package org.example.uploadservice.service;

import org.example.uploadservice.dto.AnswerRequestDto;
import org.example.uploadservice.dto.AnswerResponseDto;
import org.example.uploadservice.dto.AnswerSourceDto;
import org.example.uploadservice.dto.SemanticSearchResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionAnswerService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;
    private static final double DEFAULT_MIN_SIMILARITY = 0.45;

    private final SemanticSearchService semanticSearchService;
    private final OllamaChatService ollamaChatService;
    private final int maxContextCharactersPerResult;

    public QuestionAnswerService(
            SemanticSearchService semanticSearchService,
            OllamaChatService ollamaChatService,
            @Value("${answer.max-context-characters-per-result}") int maxContextCharactersPerResult
    ) {
        this.semanticSearchService = semanticSearchService;
        this.ollamaChatService = ollamaChatService;
        this.maxContextCharactersPerResult = maxContextCharactersPerResult;
    }

    public AnswerResponseDto answerQuestion(AnswerRequestDto request) {
        String question = normalizeQuestion(request.question());

        int limit = resolveLimit(request.limit());
        double minSimilarity = resolveMinSimilarity(request.minSimilarity());

        List<SemanticSearchResultDto> searchResults =
                semanticSearchService.search(question, limit, minSimilarity);

        List<AnswerSourceDto> sources = buildSources(searchResults);

        if (searchResults.isEmpty()) {
            return new AnswerResponseDto(
                    question,
                    "I could not find enough information in the uploaded documents to answer this question.",
                    sources
            );
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(question, searchResults);

        String answer = ollamaChatService.generateAnswer(systemPrompt, userPrompt);

        return new AnswerResponseDto(
                question,
                answer,
                sources
        );
    }

    private String normalizeQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        return question.trim();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }

    private double resolveMinSimilarity(Double minSimilarity) {
        if (minSimilarity == null) {
            return DEFAULT_MIN_SIMILARITY;
        }

        return minSimilarity;
    }

    private String buildSystemPrompt() {
        return """
                You are a document question-answering assistant.

                Rules:
                - Answer only using the provided document context.
                - If the context does not contain the answer, say that you could not find the information in the uploaded documents.
                - Do not invent facts.
                - Keep the answer clear and concise.
                - When possible, mention source numbers like [source 1], [source 2].
                """;
    }

    private String buildUserPrompt(
            String question,
            List<SemanticSearchResultDto> searchResults
    ) {
        return """
                Question:
                %s

                Document context:
                %s

                Answer:
                """.formatted(
                question,
                buildContext(searchResults)
        );
    }

    private String buildContext(List<SemanticSearchResultDto> searchResults) {
        StringBuilder contextBuilder = new StringBuilder();

        int sourceNumber = 1;

        for (SemanticSearchResultDto result : searchResults) {
            contextBuilder.append("""
                    [source %d]
                    File: %s
                    File ID: %d
                    Chunk ID: %d
                    Chunk index: %d
                    Match type: %s

                    %s

                    """.formatted(
                    sourceNumber,
                    result.originalFilename(),
                    result.fileId(),
                    result.chunkId(),
                    result.chunkIndex(),
                    result.matchType(),
                    truncate(result.content())
            ));

            sourceNumber++;
        }

        return contextBuilder.toString();
    }

    private List<AnswerSourceDto> buildSources(List<SemanticSearchResultDto> searchResults) {
        List<AnswerSourceDto> sources = new ArrayList<>();

        int sourceNumber = 1;

        for (SemanticSearchResultDto result : searchResults) {
            sources.add(new AnswerSourceDto(
                    sourceNumber,
                    result.chunkId(),
                    result.fileId(),
                    result.chunkIndex(),
                    result.originalFilename(),
                    result.searchMode(),
                    result.matchType(),
                    result.hybridScore()
            ));

            sourceNumber++;
        }

        return sources;
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }

        if (content.length() <= maxContextCharactersPerResult) {
            return content;
        }

        return content.substring(0, maxContextCharactersPerResult) + "\n[content truncated]";
    }
}