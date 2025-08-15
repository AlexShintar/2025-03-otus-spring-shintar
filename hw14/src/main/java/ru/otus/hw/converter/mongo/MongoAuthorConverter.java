package ru.otus.hw.converter.mongo;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.mongo.MongoAuthor;

@Component
public class MongoAuthorConverter {
    public String authorToString(MongoAuthor author) {
        return "Id: %s FullName: %s".formatted(author.getId(), author.getFullName());
    }
}
