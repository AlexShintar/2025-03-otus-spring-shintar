package ru.otus.hw.repository.relation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.relation.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
