CREATE TABLE chat_messages
(
    id BIGSERIAL PRIMARY KEY,

    session_id BIGINT NOT NULL
        REFERENCES chat_sessions(id),

    role VARCHAR(20) NOT NULL,

    content TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT NOW()
);