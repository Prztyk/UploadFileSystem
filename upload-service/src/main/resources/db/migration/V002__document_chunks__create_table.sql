CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT,
    created_at TIMESTAMP
);