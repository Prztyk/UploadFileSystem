package org.example.uploadservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.uploadservice.dto.AnswerTokenUsageDto;
import org.example.uploadservice.dto.OllamaAnswerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OllamaChatService {

    private final RestClient restClient;
    private final String modelName;

    public OllamaChatService(
            @Value("${ollama.base-url}") String ollamaBaseUrl,
            @Value("${answer.model}") String modelName
    ) {
        this.restClient = RestClient.create(ollamaBaseUrl);
        this.modelName = modelName;
    }

    public OllamaAnswerDto generateAnswer(String systemPrompt, String userPrompt) {
        OllamaChatResponse response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new OllamaChatRequest(
                        modelName,
                        List.of(
                                new OllamaMessage("system", systemPrompt),
                                new OllamaMessage("user", userPrompt)
                        ),
                        false,
                        Map.of("temperature", 0.1)
                ))
                .retrieve()
                .body(OllamaChatResponse.class);

        if (response == null || response.message() == null || response.message().content() == null) {
            throw new IllegalStateException("Ollama returned empty answer response");
        }

        return new OllamaAnswerDto(
                response.message().content(),
                buildTokenUsage(response)
        );
    }

    private AnswerTokenUsageDto buildTokenUsage(OllamaChatResponse response) {
        Integer inputTokens = response.promptEvalCount();
        Integer outputTokens = response.evalCount();

        Integer totalTokens = null;
        if (inputTokens != null && outputTokens != null) {
            totalTokens = inputTokens + outputTokens;
        }

        return new AnswerTokenUsageDto(
                inputTokens,
                outputTokens,
                totalTokens,
                toMilliseconds(response.totalDuration()),
                toMilliseconds(response.promptEvalDuration()),
                toMilliseconds(response.evalDuration())
        );
    }

    private Long toMilliseconds(Long nanoseconds) {
        if (nanoseconds == null) {
            return null;
        }

        return nanoseconds / 1_000_000;
    }

    private record OllamaChatRequest(
            String model,
            List<OllamaMessage> messages,
            boolean stream,
            Map<String, Object> options
    ) {
    }

    private record OllamaMessage(
            String role,
            String content
    ) {
    }

    private record OllamaChatResponse(
            OllamaMessage message,

            @JsonProperty("total_duration")
            Long totalDuration,

            @JsonProperty("load_duration")
            Long loadDuration,

            @JsonProperty("prompt_eval_count")
            Integer promptEvalCount,

            @JsonProperty("prompt_eval_duration")
            Long promptEvalDuration,

            @JsonProperty("eval_count")
            Integer evalCount,

            @JsonProperty("eval_duration")
            Long evalDuration
    ) {
    }
}