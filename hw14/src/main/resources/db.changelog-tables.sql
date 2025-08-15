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

--changeset ash:2025-06-23-create-books-table
CREATE TABLE books (
                       id bigserial PRIMARY KEY,
                       title varchar(255) NOT NULL UNIQUE,
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

--changeset ash:2025-08-15-create-author-migrate-table
CREATE TABLE author_migrate (
                                mongo_id varchar PRIMARY KEY,
                                author_id bigint NOT NULL UNIQUE REFERENCES authors(id) ON DELETE CASCADE
);

--changeset ash:2025-08-15-create-genre-migrate-table
CREATE TABLE genre_migrate (
                               mongo_id varchar PRIMARY KEY,
                               genre_id bigint NOT NULL UNIQUE REFERENCES genres(id) ON DELETE CASCADE
);

--changeset ash:2025-08-15-create-book-migrate-table
CREATE TABLE book_migrate (
                              mongo_id varchar PRIMARY KEY,
                              book_id bigint NOT NULL UNIQUE REFERENCES books(id) ON DELETE CASCADE
);

--changeset ash:2025-08-15-create-comment-migrate-table
CREATE TABLE comment_migrate (
                                 mongo_id varchar PRIMARY KEY,
                                 comment_id bigint NOT NULL UNIQUE REFERENCES comments(id) ON DELETE CASCADE
);
