package ru.otus.hw.converter.relation;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.relation.Genre;

@Component
public class GenreConverter {
    public String genreToString(Genre genre) {
        return "Id: %d, Name: %s".formatted(genre.getId(), genre.getName());
    }
}
