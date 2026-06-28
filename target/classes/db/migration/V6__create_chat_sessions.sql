CREATE TABLE chat_sessions
(
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL
        REFERENCES users(id),

    title VARCHAR(255),

    created_at TIMESTAMP DEFAULT NOW()
);