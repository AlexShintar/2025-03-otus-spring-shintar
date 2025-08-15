package ru.otus.hw.converter.relation;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.relation.Comment;

@Component
public class CommentConverter {
    public String commentToString(Comment comment) {
        return "Id: %d, for book with Id: %d, content: %s".formatted(
                comment.getId(),
                comment.getBook().getId(),
                comment.getContent()
        );
    }
}
