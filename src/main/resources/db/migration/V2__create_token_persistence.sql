CREATE TABLE refresh_token (
       id BIGSERIAL PRIMARY KEY,
       username TEXT NOT NULL UNIQUE,
       refresh_token TEXT NOT NULL UNIQUE,
       revoked BOOLEAN NOT NULL,
       date_created TIMESTAMP NOT NULL
);