CREATE TABLE document_chunk_embeddings (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGINT NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    embedding VECTOR(1536) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_document_chunk_embeddings_chunk
        FOREIGN KEY (chunk_id)
        REFERENCES document_chunks(id),

    CONSTRAINT uq_document_chunk_embeddings_chunk_model
        UNIQUE (chunk_id, model_name)
);