package ru.otus.hw.converter.mongo;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.mongo.MongoGenre;

@Component
public class MongoGenreConverter {
    public String genreToString(MongoGenre genre) {
        return "Id: %s, Name: %s".formatted(genre.getId(), genre.getName());
    }
}
