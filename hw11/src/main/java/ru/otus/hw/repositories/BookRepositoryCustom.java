package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;

public interface BookRepositoryCustom {
    Flux<BookDto> findAllDto();

    Mono<BookDto> findBookDtoById(Long id);
}
