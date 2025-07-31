package ru.otus.hw.mapper;

import org.mapstruct.Mapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDto toDto(Comment comment);
}
