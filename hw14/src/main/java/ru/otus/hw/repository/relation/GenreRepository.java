package ru.otus.hw.repository.relation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.relation.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
