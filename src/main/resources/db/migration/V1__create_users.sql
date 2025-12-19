CREATE TABLE users (
       id BIGSERIAL PRIMARY KEY,
       email TEXT NOT NULL UNIQUE,
       name TEXT NOT NULL UNIQUE,
       password_hash TEXT NOT NULL,
       created_at TIMESTAMP NOT NULL
);