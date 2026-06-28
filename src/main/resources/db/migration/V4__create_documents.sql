CREATE TABLE documents
(
    id BIGSERIAL PRIMARY KEY,

    file_name VARCHAR(255) NOT NULL,

    content_type VARCHAR(100),

    uploaded_by BIGINT
        REFERENCES users(id),

    department_id BIGINT
        REFERENCES departments(id),

    created_at TIMESTAMP DEFAULT NOW()
);