package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;

public interface BookService {
    Mono<BookDto> findById(long id);

    Flux<BookDto> findAll();

    Mono<BookDto> insert(BookCreateDto form);

    Mono<BookDto> update(BookUpdateDto form, long id);

    Mono<Void> deleteById(long id);
}
