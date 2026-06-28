package org.example.uploadservice.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmbeddingPersistenceService {

    private final JdbcTemplate jdbcTemplate;
    private final PgVectorFormatService pgVectorFormatService;

    public EmbeddingPersistenceService(
            JdbcTemplate jdbcTemplate,
            PgVectorFormatService pgVectorFormatService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.pgVectorFormatService = pgVectorFormatService;
    }

    public void saveEmbedding(Long chunkId, String modelName, List<Double> embedding) {
        String vectorValue = pgVectorFormatService.toPgVectorValue(embedding);

        jdbcTemplate.update(
                """
                INSERT INTO document_chunk_embeddings (
                    chunk_id,
                    model_name,
                    embedding,
                    created_at
                )
                VALUES (?, ?, ?::vector, ?)
                ON CONFLICT (chunk_id, model_name)
                DO UPDATE SET
                    embedding = EXCLUDED.embedding,
                    created_at = EXCLUDED.created_at
                """,
                chunkId,
                modelName,
                vectorValue,
                LocalDateTime.now()
        );
    }
}