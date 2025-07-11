package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Override
    @NonNull
    @EntityGraph(value = "book:author-only-entity-graph")
    List<Book> findAll();

    @Override
    @NonNull
    @EntityGraph(value = "book:author-genre-entity-graph")
    Optional<Book> findById(@NonNull Long id);
}