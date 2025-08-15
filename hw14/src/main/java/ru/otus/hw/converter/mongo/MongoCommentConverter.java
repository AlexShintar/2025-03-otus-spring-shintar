package ru.otus.hw.converter.mongo;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.mongo.MongoComment;

@Component
public class MongoCommentConverter {
    public String commentToString(MongoComment comment) {
        return "Id: %s, for book: %s, content: %s".formatted(
                comment.getId(),
                comment.getBook(),
                comment.getBook()
        );
    }
}