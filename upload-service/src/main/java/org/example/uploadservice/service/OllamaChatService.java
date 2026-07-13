package org.example.uploadservice.service;

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

    public String generateAnswer(String systemPrompt, String userPrompt) {
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

        return response.message().content();
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
            OllamaMessage message
    ) {
    }
}