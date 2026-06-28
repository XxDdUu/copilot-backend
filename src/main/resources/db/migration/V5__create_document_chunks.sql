CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,

    document_id BIGINT NOT NULL,

    chunk_index INT NOT NULL,

    content TEXT NOT NULL,

    embedding VECTOR(1536),

    created_at TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (document_id)
        REFERENCES documents(id)
);