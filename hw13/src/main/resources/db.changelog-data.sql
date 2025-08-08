--liquibase formatted sql

--changeset ash:2025-06-23-insert-authors
INSERT INTO authors (full_name) VALUES
                                    ('Author_1'),
                                    ('Author_2'),
                                    ('Author_3');

--changeset ash:2025-06-23-insert-genres
INSERT INTO genres (name) VALUES
                              ('Genre_1'),
                              ('Genre_2'),
                              ('Genre_3'),
                              ('Genre_4'),
                              ('Genre_5'),
                              ('Genre_6');

--changeset ash:2025-06-23-insert-books
INSERT INTO books (title, author_id) VALUES
                                         ('BookTitle_1',1),
                                         ('BookTitle_2',2),
                                         ('BookTitle_3',3);

--changeset ash:2025-06-23-insert-books-genres
INSERT INTO books_genres (book_id, genre_id) VALUES
                                                 (1,1),(1,2),(1,3),
                                                 (2,4),
                                                 (3,5),(3,6);

--changeset ash:2025-06-23-insert-comments
INSERT INTO comments (comment_content, book_id) VALUES
                                                    ('Comment_1_for_BookTitle_1',1),
                                                    ('Comment_2_for_BookTitle_1',1),
                                                    ('Comment_1_for_BookTitle_2',2),
                                                    ('Comment_2_for_BookTitle_2',2),
                                                    ('Comment_1_for_BookTitle_3',3);

--changeset ash:2025-07-15-insert-users
INSERT INTO users (user_name, user_password) VALUES
                                           ('user',  '$2a$12$R8t9stj5Ozg87sIWWfO58.5VV.iOnLkAZTTIwIsn4LbLHLrtyiSay'),
                                           ('admin', '$2a$12$XC512r3lxHhXLo72C06s0e5OLLGuiGT59F0Qx3VIuYNB7y.oss4QW');
