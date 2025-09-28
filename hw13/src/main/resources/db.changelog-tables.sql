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

--changeset ash:2025-08-08-create-users-table
CREATE TABLE users (
                          id bigserial PRIMARY KEY,
                          user_name varchar(255) NOT NULL UNIQUE,
                          user_password varchar(255) NOT NULL,
                          user_role varchar(50) NOT NULL
);

--changeset ash:2025-08-08-create-acl-sid
CREATE TABLE IF NOT EXISTS acl_sid (
                                       id bigserial PRIMARY KEY,
                                       principal boolean NOT NULL,
                                       sid varchar(100) NOT NULL,
                                       CONSTRAINT unique_uk_1 UNIQUE (sid, principal)
);

--changeset ash:2025-08-08-create-acl-class
CREATE TABLE IF NOT EXISTS acl_class (
                                         id bigserial PRIMARY KEY,
                                         class varchar(255) NOT NULL,
                                         CONSTRAINT unique_uk_2 UNIQUE (class)
);

--changeset ash:2025-08-08-create-acl-object-identity
CREATE TABLE IF NOT EXISTS acl_object_identity (
                                                   id bigserial PRIMARY KEY,
                                                   object_id_class bigint NOT NULL,
                                                   object_id_identity bigint NOT NULL,
                                                   parent_object bigint,
                                                   owner_sid bigint,
                                                   entries_inheriting boolean NOT NULL,
                                                   CONSTRAINT unique_uk_3 UNIQUE (object_id_class, object_id_identity)
);

--changeset ash:2025-08-08-create-acl-entry
CREATE TABLE IF NOT EXISTS acl_entry (
                                         id bigserial PRIMARY KEY,
                                         acl_object_identity bigint NOT NULL,
                                         ace_order int NOT NULL,
                                         sid bigint NOT NULL,
                                         mask int NOT NULL,
                                         granting boolean NOT NULL,
                                         audit_success boolean NOT NULL,
                                         audit_failure boolean NOT NULL,
                                         CONSTRAINT unique_uk_4 UNIQUE (acl_object_identity, ace_order)
);

--changeset ash:2025-08-08-add-fk-acl-entry
ALTER TABLE acl_entry
    ADD CONSTRAINT fk_acl_entry_object_identity
        FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity(id);

ALTER TABLE acl_entry
    ADD CONSTRAINT fk_acl_entry_sid
        FOREIGN KEY (sid) REFERENCES acl_sid(id);

--changeset ash:2025-08-08-add-fk-acl-object-identity
ALTER TABLE acl_object_identity
    ADD CONSTRAINT fk_acl_object_identity_parent
        FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id);

ALTER TABLE acl_object_identity
    ADD CONSTRAINT fk_acl_object_identity_class
        FOREIGN KEY (object_id_class) REFERENCES acl_class (id);

ALTER TABLE acl_object_identity
    ADD CONSTRAINT fk_acl_object_identity_owner_sid
        FOREIGN KEY (owner_sid) REFERENCES acl_sid (id);
