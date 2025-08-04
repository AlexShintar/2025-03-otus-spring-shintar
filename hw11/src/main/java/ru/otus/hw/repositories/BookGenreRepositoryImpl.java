package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookGenreRepositoryImpl implements BookGenreRepository {
    private static final String SQL_INSERT_BOOK_GENRES_LINK = """
            INSERT INTO books_genres (book_id, genre_id) VALUES (:bookId, :genreId)
            """;

    private static final String SQL_DELETE_BOOK_GENRES_LINK = """
            DELETE FROM books_genres WHERE book_id = :bookId
            """;

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Void> linkGenresToBook(long bookId, List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(genreIds)
                .flatMap(genreId -> template.getDatabaseClient()
                        .sql(SQL_INSERT_BOOK_GENRES_LINK)
                        .bind("bookId", bookId)
                        .bind("genreId", genreId)
                        .then())
                .then();
    }

    @Override
    public Mono<Void> deleteLinksByBookId(long bookId) {
        return template.getDatabaseClient()
                .sql(SQL_DELETE_BOOK_GENRES_LINK)
                .bind("bookId", bookId)
                .then();
    }
}
