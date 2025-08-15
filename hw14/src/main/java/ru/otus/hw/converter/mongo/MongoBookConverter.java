package ru.otus.hw.converter.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.mongo.MongoBook;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MongoBookConverter {
    private final MongoAuthorConverter authorConverter;

    private final MongoGenreConverter genreConverter;

    public String bookToString(MongoBook book) {
        var genresString = book.getGenres().stream()
                .map(genreConverter::genreToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        return "Id: %s, title: %s, author: {%s}, genres: [%s]".formatted(
                book.getId(),
                book.getTitle(),
                authorConverter.authorToString(book.getAuthor()),
                genresString);
    }
}
