CREATE TABLE audit_logs
(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT
        REFERENCES users(id),

    action VARCHAR(255) NOT NULL,

    details TEXT,

    created_at TIMESTAMP DEFAULT NOW()
);