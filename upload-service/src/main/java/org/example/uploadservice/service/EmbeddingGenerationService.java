package org.example.uploadservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class EmbeddingGenerationService {

    private static final int EMBEDDING_DIMENSION = 1536;

    public List<Double> generateEmbedding(String text) {
        Random random = new Random(text.hashCode());

        List<Double> embedding = new ArrayList<>();

        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            embedding.add(random.nextDouble(-1.0, 1.0));
        }

        return embedding;
    }
}