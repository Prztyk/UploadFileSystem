CREATE TABLE uploaded_files (
    id BIGSERIAL PRIMARY KEY,
    original_filename VARCHAR(255),
    stored_filename VARCHAR(255),
    content_type VARCHAR(255),
    size BIGINT,
    status VARCHAR(50),
    created_at TIMESTAMP
);