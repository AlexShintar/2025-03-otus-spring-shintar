package ru.otus.hw.shell.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converter.relation.CommentConverter;
import ru.otus.hw.repository.relation.CommentRepository;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class CommentCommands {

    private final CommentRepository commentRepository;

    private final CommentConverter commentConverter;

    @ShellMethod(value = "Find all comments by book id", key = "acb")
    public String findAllCommentsByBookId(long bookId) {
        return commentRepository.findAllByBookId(bookId).stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

}
