CREATE TABLE uploaded_file_processing_logs (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    status VARCHAR(50),
    message TEXT,
    stack_trace TEXT,
    created_at TIMESTAMP
);