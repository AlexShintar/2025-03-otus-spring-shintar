package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.otus.hw.models.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, Long>, BookRepositoryCustom {
}