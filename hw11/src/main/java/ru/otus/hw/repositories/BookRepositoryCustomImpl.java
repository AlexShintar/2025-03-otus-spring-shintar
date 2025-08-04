package ru.otus.hw.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {
    private static final String SQL_ALL = """
            SELECT
                b.id,
                b.title,
                b.author_id,
                a.full_name AS author_name,
                JSON_ARRAYAGG(
                    JSON_OBJECT('id' VALUE g.id, 'name' VALUE g.name)
                ) AS genres
            FROM books b
            LEFT JOIN authors a ON b.author_id = a.id
            LEFT JOIN books_genres bg ON b.id = bg.book_id
            LEFT JOIN genres g ON g.id = bg.genre_id
            GROUP BY b.id, b.title, b.author_id, a.full_name
            """;

    private static final String SQL_FIND_BY_ID = """
        SELECT
            b.id,
            b.title,
            b.author_id,
            a.full_name AS author_name,
            JSON_ARRAYAGG(
                JSON_OBJECT('id' VALUE g.id, 'name' VALUE g.name)
            ) AS genres
        FROM books b
        LEFT JOIN authors a ON b.author_id = a.id
        LEFT JOIN books_genres bg ON b.id = bg.book_id
        LEFT JOIN genres g ON g.id = bg.genre_id
        WHERE b.id = :id
        GROUP BY b.id, b.title, b.author_id, a.full_name
        """;

    private final R2dbcEntityTemplate template;

    private final ObjectMapper objectMapper;

    @Override
    public Flux<BookDto> findAllDto() {
        return template.getDatabaseClient()
                .sql(SQL_ALL)
                .map(this::mapToBookDto)
                .all();
    }

    @Override
    public Mono<BookDto> findBookDtoById(Long id) {
        return template.getDatabaseClient()
                .sql(SQL_FIND_BY_ID)
                .bind("id", id)
                .map(this::mapToBookDto)
                .one();
    }

    private BookDto mapToBookDto(Readable selectedRecord) {
        String genresJson = selectedRecord.get("genres", String.class);
        try {
            List<GenreDto> genres = (genresJson == null)
                    ? Collections.emptyList()
                    : objectMapper.readValue(genresJson, new TypeReference<>() {
            });

            AuthorDto author = new AuthorDto(
                    selectedRecord.get("author_id", Long.class),
                    selectedRecord.get("author_name", String.class)
            );

            return new BookDto(
                    selectedRecord.get("id", Long.class),
                    selectedRecord.get("title", String.class),
                    author,
                    genres
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("genres:" + genresJson + " parsing error:" + e);
        }
    }
}
