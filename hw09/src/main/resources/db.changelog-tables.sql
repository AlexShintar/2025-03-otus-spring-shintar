--liquibase formatted sql

--changeset ash:2025-06-23-create-authors-table
CREATE TABLE authors (
                         id bigserial PRIMARY KEY,
                         full_name varchar(255) NOT NULL
);

--changeset ash:2025-06-23-create-genres-table
CREATE TABLE genres (
                        id bigserial PRIMARY KEY,
                        name varchar(255) NOT NULL UNIQUE
);

--changeset ash:2025-07-07-create-books-table
CREATE TABLE books (
                       id bigserial PRIMARY KEY,
                       title varchar(255) NOT NULL,
                       author_id bigint REFERENCES authors(id) ON DELETE CASCADE
);

--changeset ash:2025-06-23-create-books-genres-table
CREATE TABLE books_genres (
                              book_id bigint NOT NULL,
                              genre_id bigint NOT NULL,
                              PRIMARY KEY (book_id, genre_id)
);
ALTER TABLE books_genres
    ADD CONSTRAINT fk_books_genres_book
        FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE;
ALTER TABLE books_genres
    ADD CONSTRAINT fk_books_genres_genre
        FOREIGN KEY (genre_id) REFERENCES genres(id);

--changeset ash:2025-06-23-create-comments-table
CREATE TABLE comments (
                          id bigserial PRIMARY KEY,
                          comment_content varchar(1000) NOT NULL,
                          book_id bigint REFERENCES books(id) ON DELETE CASCADE
);
