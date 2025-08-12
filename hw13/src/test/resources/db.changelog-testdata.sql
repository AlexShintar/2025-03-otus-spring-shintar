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

--changeset ash:2025-08-08-insert-users
INSERT INTO users (user_name, user_password, user_role) VALUES
                                                            ('vetinari',  '$2a$12$GRVH0AP6MUIyvubu59zLguDWbM79n9g3arCVcFheVjC8zpBiYR6Pu', 'ADMIN'),
                                                            ('nobby', '$2a$12$4VA1AYpHB7.bM4JRcXGDWOCm/TpYy8fCCISicIKCbKKntoGCtSKZ2', 'USER');

--changeset ash:2025-08-08-acl-sids
INSERT INTO acl_sid (principal, sid) VALUES (TRUE, 'vetinari');
INSERT INTO acl_sid (principal, sid) VALUES (TRUE, 'nobby');

INSERT INTO acl_sid (principal, sid) VALUES (FALSE, 'ROLE_USER');
INSERT INTO acl_sid (principal, sid) VALUES (FALSE, 'ROLE_ADMIN');

--changeset ash:2025-08-08-acl-classes
INSERT INTO acl_class (class) VALUES ('ru.otus.hw.models.Book');
INSERT INTO acl_class (class) VALUES ('ru.otus.hw.models.Comment');

--changeset ash:2025-08-08-acl-oi-books
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book'), 1, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), TRUE);
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book'), 2, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), TRUE);
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book'), 3, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), TRUE);

--changeset ash:2025-08-08-acl-oi-comments
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment'), 1, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), TRUE);
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment'), 2, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='nobby'), TRUE);
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment'), 3, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), TRUE);
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment'), 4, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='nobby'), TRUE);
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES ((SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment'), 5, NULL, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), TRUE);

--changeset ash:2025-08-08-acl-entries-books
-- Book id = 1
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=1), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'),  1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=1), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=1), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=1), 3, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=1), 4, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 4, TRUE, FALSE, FALSE);

-- Book id = 2
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=2), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'),  1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=2), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=2), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=2), 3, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=2), 4, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 4, TRUE, FALSE, FALSE);

-- Book id = 3
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=3), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'),  1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=3), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=3), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=3), 3, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Book') AND object_id_identity=3), 4, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 4, TRUE, FALSE, FALSE);

--changeset ash:2025-08-08-acl-entries-comments
-- Comment id = 1
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=1), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=1), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=1), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=1), 3, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=1), 4, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=1), 5, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);

-- Comment id = 2
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=2), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=2), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=2), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=2), 3, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='nobby'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=2), 4, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='nobby'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=2), 5, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);

-- Comment id = 3
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=3), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=3), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=3), 2, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=3), 3, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=3), 4, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);

-- Comment id = 4
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=4), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=4), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=4), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=4), 3, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='nobby'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=4), 4, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='nobby'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=4), 5, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);

-- Comment id = 5
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=5), 0, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_USER'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=5), 1, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=5), 2, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=5), 3, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), 8, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=5), 4, (SELECT id FROM acl_sid WHERE principal=TRUE AND sid='vetinari'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    ((SELECT id FROM acl_object_identity WHERE object_id_class=(SELECT id FROM acl_class WHERE class='ru.otus.hw.models.Comment') AND object_id_identity=5), 5, (SELECT id FROM acl_sid WHERE principal=FALSE AND sid='ROLE_ADMIN'), 2, TRUE, FALSE, FALSE);
