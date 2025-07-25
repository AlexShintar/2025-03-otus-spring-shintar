package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Component
public class CommentConverter {

    public CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getContent());
    }
}
