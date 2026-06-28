package org.example.uploadservice.service;

import org.example.uploadservice.dto.SemanticSearchResultDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemanticSearchService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingGenerationService embeddingGenerationService;
    private final PgVectorFormatService pgVectorFormatService;

    public SemanticSearchService(
            JdbcTemplate jdbcTemplate,
            EmbeddingGenerationService embeddingGenerationService,
            PgVectorFormatService pgVectorFormatService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingGenerationService = embeddingGenerationService;
        this.pgVectorFormatService = pgVectorFormatService;
    }

    public List<SemanticSearchResultDto> search(String query, int limit) {
        List<Double> queryEmbedding = embeddingGenerationService.generateEmbedding(query);
        String queryVector = pgVectorFormatService.toPgVectorValue(queryEmbedding);
        String modelName = embeddingGenerationService.getModelName();

        return jdbcTemplate.query(
                """
                SELECT
                    dc.id AS chunk_id,
                    dc.file_id,
                    dc.chunk_index,
                    uf.original_filename,
                    dc.content,
                    dce.embedding <=> ?::vector AS distance
                FROM document_chunk_embeddings dce
                JOIN document_chunks dc ON dc.id = dce.chunk_id
                JOIN uploaded_files uf ON uf.id = dc.file_id
                WHERE dce.model_name = ?
                ORDER BY dce.embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new SemanticSearchResultDto(
                        rs.getLong("chunk_id"),
                        rs.getLong("file_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("original_filename"),
                        rs.getString("content"),
                        rs.getDouble("distance")
                ),
                queryVector,
                modelName,
                queryVector,
                limit
        );
    }
}