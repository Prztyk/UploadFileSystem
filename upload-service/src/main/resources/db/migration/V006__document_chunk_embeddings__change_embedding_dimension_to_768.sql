DELETE FROM document_chunk_embeddings;

ALTER TABLE document_chunk_embeddings
    ALTER COLUMN embedding TYPE VECTOR(768);