package org.example.uploadservice.service;

import org.example.uploadservice.dto.EmbeddingStatusDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingStatusService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingGenerationService embeddingGenerationService;

    public EmbeddingStatusService(
            JdbcTemplate jdbcTemplate,
            EmbeddingGenerationService embeddingGenerationService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingGenerationService = embeddingGenerationService;
    }

    public EmbeddingStatusDto getEmbeddingStatus(Long fileId) {
        String modelName = embeddingGenerationService.getModelName();

        return jdbcTemplate.queryForObject(
                """
                SELECT
                    COUNT(dc.id) AS chunk_count,
                    COUNT(dce.id) AS embedding_count
                FROM document_chunks dc
                LEFT JOIN document_chunk_embeddings dce
                    ON dce.chunk_id = dc.id
                   AND dce.model_name = ?
                WHERE dc.file_id = ?
                """,
                (rs, rowNum) -> {
                    long chunkCount = rs.getLong("chunk_count");
                    long embeddingCount = rs.getLong("embedding_count");
                    long missingEmbeddingCount = chunkCount - embeddingCount;

                    return new EmbeddingStatusDto(
                            fileId,
                            modelName,
                            chunkCount,
                            embeddingCount,
                            missingEmbeddingCount,
                            chunkCount > 0 && missingEmbeddingCount == 0
                    );
                },
                modelName,
                fileId
        );
    }
}