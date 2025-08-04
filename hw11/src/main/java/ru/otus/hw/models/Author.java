package ru.otus.hw.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("authors")
public record Author(@Id Long id, @Column("full_name") String fullName) {
}
