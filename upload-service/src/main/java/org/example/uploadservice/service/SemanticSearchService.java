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

        return jdbcTemplate.query(
                """
                WITH ranked_chunks AS (
                    SELECT
                        dc.id AS chunk_id,
                        dc.file_id,
                        dc.chunk_index,
                        uf.original_filename,
                        previous_chunk.content AS previous_content,
                        dc.content,
                        next_chunk.content AS next_content,
                        dce.embedding <=> ?::vector AS distance,
                        ts_rank_cd(
                            dc.search_vector,
                            websearch_to_tsquery('english', ?)
                        ) AS lexical_score,
                        dc.search_vector @@ phraseto_tsquery('english', ?) AS exact_phrase_match
                    FROM document_chunk_embeddings dce
                    JOIN document_chunks dc ON dc.id = dce.chunk_id
                    JOIN uploaded_files uf ON uf.id = dc.file_id
    
                    LEFT JOIN document_chunks previous_chunk
                        ON previous_chunk.file_id = dc.file_id
                       AND previous_chunk.chunk_index = dc.chunk_index - 1
    
                    LEFT JOIN document_chunks next_chunk
                        ON next_chunk.file_id = dc.file_id
                       AND next_chunk.chunk_index = dc.chunk_index + 1
    
                    WHERE dce.model_name = ?
                ),
                scored_chunks AS (
                    SELECT
                        chunk_id,
                        file_id,
                        chunk_index,
                        original_filename,
                        previous_content,
                        content,
                        next_content,
                        distance,
                        1 - distance AS similarity_score,
                        lexical_score,
                        exact_phrase_match,
                        (
                            ((1 - distance) * 0.70)
                            + (least(lexical_score, 1.0) * 0.25)
                            + CASE WHEN exact_phrase_match THEN 0.20 ELSE 0 END
                        ) AS hybrid_score
                    FROM ranked_chunks
                )
                SELECT
                    chunk_id,
                    file_id,
                    chunk_index,
                    original_filename,
                    previous_content,
                    content,
                    next_content,
                    distance,
                    similarity_score,
                    lexical_score,
                    exact_phrase_match,
                    hybrid_score
                FROM scored_chunks
                WHERE similarity_score >= ?
                   OR lexical_score > 0
                   OR exact_phrase_match = true
                ORDER BY hybrid_score DESC
                LIMIT ?
                """,
                (rs, rowNum) -> new SemanticSearchResultDto(
                        rs.getLong("chunk_id"),
                        rs.getLong("file_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("original_filename"),
                        rs.getString("previous_content"),
                        rs.getString("content"),
                        rs.getString("next_content"),
                        rs.getDouble("distance"),
                        rs.getDouble("similarity_score"),
                        rs.getDouble("lexical_score"),
                        rs.getBoolean("exact_phrase_match"),
                        rs.getDouble("hybrid_score")
                ),
                queryVector,
                query,
                query,
                modelName,
                minSimilarity,
                limit
        );
    }
}