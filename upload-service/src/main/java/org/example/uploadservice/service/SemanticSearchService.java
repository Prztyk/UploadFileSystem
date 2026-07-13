package org.example.uploadservice.service;

import org.example.uploadservice.dto.SemanticSearchResultDto;
import org.example.uploadservice.enums.SearchMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemanticSearchService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingGenerationService embeddingGenerationService;
    private final PgVectorFormatService pgVectorFormatService;
    private final SearchQueryAnalyzerService searchQueryAnalyzerService;

    public SemanticSearchService(
            JdbcTemplate jdbcTemplate,
            EmbeddingGenerationService embeddingGenerationService,
            PgVectorFormatService pgVectorFormatService,
            SearchQueryAnalyzerService searchQueryAnalyzerService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingGenerationService = embeddingGenerationService;
        this.pgVectorFormatService = pgVectorFormatService;
        this.searchQueryAnalyzerService = searchQueryAnalyzerService;
    }

    public List<SemanticSearchResultDto> search(String query, int limit, double minSimilarity) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        SearchMode searchMode = searchQueryAnalyzerService.determineSearchMode(query);
        String normalizedQuery = searchQueryAnalyzerService.normalizeQuery(query);

        return switch (searchMode) {
            case EXACT_PHRASE -> searchExactPhrase(normalizedQuery, limit);
            case LEXICAL_ONLY -> searchLexicalOnly(normalizedQuery, limit);
            case HYBRID -> searchHybrid(normalizedQuery, limit, minSimilarity);
        };
    }

    private List<SemanticSearchResultDto> searchHybrid(
            String query,
            int limit,
            double minSimilarity
    ) {
        List<Double> queryEmbedding = embeddingGenerationService.generateEmbedding(query);
        String queryVector = pgVectorFormatService.toPgVectorValue(queryEmbedding);
        String modelName = embeddingGenerationService.getModelName();
        String exactPhrasePattern = "%" + query.toLowerCase() + "%";

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
                        (
                            lower(dc.content) LIKE ?
                            OR dc.search_vector @@ phraseto_tsquery('english', ?)
                        ) AS exact_phrase_match
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
                            ((1 - distance) * 0.65)
                            + (least(lexical_score, 1.0) * 0.25)
                            + CASE WHEN exact_phrase_match THEN 0.25 ELSE 0 END
                        ) AS hybrid_score,
                        CASE
                            WHEN exact_phrase_match THEN 'EXACT_PHRASE'
                            WHEN lexical_score > 0 AND (1 - distance) >= ? THEN 'HYBRID'
                            WHEN lexical_score > 0 THEN 'LEXICAL'
                            ELSE 'SEMANTIC'
                        END AS match_type
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
                    hybrid_score,
                    'HYBRID' AS search_mode,
                    match_type
                FROM scored_chunks
                WHERE exact_phrase_match = true
                   OR lexical_score > 0
                   OR similarity_score >= ?
                ORDER BY hybrid_score DESC
                LIMIT ?
                """,
                this::mapSearchResult,
                queryVector,
                query,
                exactPhrasePattern,
                query,
                modelName,
                minSimilarity,
                minSimilarity,
                limit
        );
    }

    private List<SemanticSearchResultDto> searchLexicalOnly(String query, int limit) {
        String exactPhrasePattern = "%" + query.toLowerCase() + "%";

        return jdbcTemplate.query(
                """
                WITH lexical_chunks AS (
                    SELECT
                        dc.id AS chunk_id,
                        dc.file_id,
                        dc.chunk_index,
                        uf.original_filename,
                        previous_chunk.content AS previous_content,
                        dc.content,
                        next_chunk.content AS next_content,
                        ts_rank_cd(
                            dc.search_vector,
                            websearch_to_tsquery('english', ?)
                        ) AS lexical_score,
                        (
                            lower(dc.content) LIKE ?
                            OR dc.search_vector @@ phraseto_tsquery('english', ?)
                        ) AS exact_phrase_match
                    FROM document_chunks dc
                    JOIN uploaded_files uf ON uf.id = dc.file_id

                    LEFT JOIN document_chunks previous_chunk
                        ON previous_chunk.file_id = dc.file_id
                       AND previous_chunk.chunk_index = dc.chunk_index - 1

                    LEFT JOIN document_chunks next_chunk
                        ON next_chunk.file_id = dc.file_id
                       AND next_chunk.chunk_index = dc.chunk_index + 1

                    WHERE dc.search_vector @@ websearch_to_tsquery('english', ?)
                       OR dc.search_vector @@ phraseto_tsquery('english', ?)
                       OR lower(dc.content) LIKE ?
                )
                SELECT
                    chunk_id,
                    file_id,
                    chunk_index,
                    original_filename,
                    previous_content,
                    content,
                    next_content,
                    NULL::double precision AS distance,
                    NULL::double precision AS similarity_score,
                    lexical_score,
                    exact_phrase_match,
                    (
                        least(lexical_score, 1.0)
                        + CASE WHEN exact_phrase_match THEN 0.50 ELSE 0 END
                    ) AS hybrid_score,
                    'LEXICAL_ONLY' AS search_mode,
                    CASE
                        WHEN exact_phrase_match THEN 'EXACT_PHRASE'
                        ELSE 'LEXICAL'
                    END AS match_type
                FROM lexical_chunks
                ORDER BY
                    exact_phrase_match DESC,
                    lexical_score DESC
                LIMIT ?
                """,
                this::mapSearchResult,
                query,
                exactPhrasePattern,
                query,
                query,
                query,
                exactPhrasePattern,
                limit
        );
    }

    private List<SemanticSearchResultDto> searchExactPhrase(String query, int limit) {
        String exactPhrasePattern = "%" + query.toLowerCase() + "%";

        return jdbcTemplate.query(
                """
                SELECT
                    dc.id AS chunk_id,
                    dc.file_id,
                    dc.chunk_index,
                    uf.original_filename,
                    previous_chunk.content AS previous_content,
                    dc.content,
                    next_chunk.content AS next_content,
                    NULL::double precision AS distance,
                    NULL::double precision AS similarity_score,
                    ts_rank_cd(
                        dc.search_vector,
                        phraseto_tsquery('english', ?)
                    ) AS lexical_score,
                    true AS exact_phrase_match,
                    1.0 AS hybrid_score,
                    'EXACT_PHRASE' AS search_mode,
                    'EXACT_PHRASE' AS match_type
                FROM document_chunks dc
                JOIN uploaded_files uf ON uf.id = dc.file_id

                LEFT JOIN document_chunks previous_chunk
                    ON previous_chunk.file_id = dc.file_id
                   AND previous_chunk.chunk_index = dc.chunk_index - 1

                LEFT JOIN document_chunks next_chunk
                    ON next_chunk.file_id = dc.file_id
                   AND next_chunk.chunk_index = dc.chunk_index + 1

                WHERE lower(dc.content) LIKE ?
                   OR dc.search_vector @@ phraseto_tsquery('english', ?)

                ORDER BY dc.file_id, dc.chunk_index
                LIMIT ?
                """,
                this::mapSearchResult,
                query,
                exactPhrasePattern,
                query,
                limit
        );
    }

    private SemanticSearchResultDto mapSearchResult(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new SemanticSearchResultDto(
                rs.getLong("chunk_id"),
                rs.getLong("file_id"),
                rs.getInt("chunk_index"),
                rs.getString("original_filename"),
                rs.getString("previous_content"),
                rs.getString("content"),
                rs.getString("next_content"),
                getNullableDouble(rs, "distance"),
                getNullableDouble(rs, "similarity_score"),
                getNullableDouble(rs, "lexical_score"),
                rs.getBoolean("exact_phrase_match"),
                getNullableDouble(rs, "hybrid_score"),
                rs.getString("search_mode"),
                rs.getString("match_type")
        );
    }

    private Double getNullableDouble(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        double value = rs.getDouble(columnName);

        if (rs.wasNull()) {
            return null;
        }

        return value;
    }
}