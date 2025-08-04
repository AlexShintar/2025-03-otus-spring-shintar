package ru.otus.hw.repositories;

import reactor.core.publisher.Mono;

import java.util.List;

public interface BookGenreRepository {
    Mono<Void> linkGenresToBook(long bookId, List<Long> genreIds);

    Mono<Void> deleteLinksByBookId(long bookId);
}
