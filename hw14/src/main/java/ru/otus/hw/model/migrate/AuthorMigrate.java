package ru.otus.hw.model.migrate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "author_migrate")
public class AuthorMigrate {
    @Id
    @Column(name = "mongo_id")
    private String mongoId;

    @Column(name = "author_id")
    private Long authorId;
}
