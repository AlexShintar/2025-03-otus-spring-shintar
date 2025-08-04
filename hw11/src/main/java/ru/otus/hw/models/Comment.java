package ru.otus.hw.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("comments")
public record Comment(@Id Long id, @Column("comment_content") String content, @Column("book_id") Long bookId) {
}
