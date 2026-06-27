package org.example.uploadservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class EmbeddingGenerationService {

    private final RestClient restClient;
    private final String modelName;
    private final int expectedDimension;

    public EmbeddingGenerationService(
            @Value("${ollama.base-url}") String ollamaBaseUrl,
            @Value("${embedding.model}") String modelName,
            @Value("${embedding.dimension}") int expectedDimension
    ) {
        this.restClient = RestClient.create(ollamaBaseUrl);
        this.modelName = modelName;
        this.expectedDimension = expectedDimension;
    }

    public List<Double> generateEmbedding(String text) {
        OllamaEmbedResponse response = restClient.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new OllamaEmbedRequest(modelName, text))
                .retrieve()
                .body(OllamaEmbedResponse.class);

        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new IllegalStateException("Ollama returned empty embedding response");
        }

        List<Double> embedding = response.embeddings().get(0);

        if (embedding.size() != expectedDimension) {
            throw new IllegalStateException(
                    "Unexpected embedding dimension. Expected "
                            + expectedDimension
                            + " but got "
                            + embedding.size()
            );
        }

        return embedding;
    }

    public String getModelName() {
        return modelName;
    }

    private record OllamaEmbedRequest(
            String model,
            String input
    ) {
    }

    private record OllamaEmbedResponse(
            List<List<Double>> embeddings
    ) {
    }
}