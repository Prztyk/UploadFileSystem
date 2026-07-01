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

    public List<SemanticSearchResultDto> search(String query, int limit, double minSimilarity) {
        List<Double> queryEmbedding = embeddingGenerationService.generateEmbedding(query);
        String queryVector = pgVectorFormatService.toPgVectorValue(queryEmbedding);
        String modelName = embeddingGenerationService.getModelName();

        String phrasePattern = "%" + query.toLowerCase() + "%";

        return jdbcTemplate.query(
                """
                WITH ranked_chunks AS (
                    SELECT
                        dc.id AS chunk_id,
                        dc.file_id,
                        dc.chunk_index,
                        uf.original_filename,
                        dc.content,
                        dce.embedding <=> ?::vector AS distance,
                        CASE
                            WHEN lower(dc.content) LIKE ? THEN true
                            ELSE false
                        END AS exact_phrase_match
                    FROM document_chunk_embeddings dce
                    JOIN document_chunks dc ON dc.id = dce.chunk_id
                    JOIN uploaded_files uf ON uf.id = dc.file_id
                    WHERE dce.model_name = ?
                ),
                scored_chunks AS (
                    SELECT
                        chunk_id,
                        file_id,
                        chunk_index,
                        original_filename,
                        content,
                        distance,
                        1 - distance AS similarity_score,
                        exact_phrase_match,
                        CASE
                            WHEN exact_phrase_match THEN (1 - distance) + 0.30
                            ELSE 1 - distance
                        END AS hybrid_score
                    FROM ranked_chunks
                )
                SELECT
                    chunk_id,
                    file_id,
                    chunk_index,
                    original_filename,
                    content,
                    distance,
                    similarity_score,
                    exact_phrase_match,
                    hybrid_score
                FROM scored_chunks
                WHERE similarity_score >= ?
                ORDER BY hybrid_score DESC
                LIMIT ?
                """,
                (rs, rowNum) -> new SemanticSearchResultDto(
                        rs.getLong("chunk_id"),
                        rs.getLong("file_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("original_filename"),
                        rs.getString("content"),
                        rs.getDouble("distance"),
                        rs.getDouble("similarity_score"),
                        rs.getBoolean("exact_phrase_match"),
                        rs.getDouble("hybrid_score")
                ),
                queryVector,
                phrasePattern,
                modelName,
                minSimilarity,
                limit
        );
    }
}