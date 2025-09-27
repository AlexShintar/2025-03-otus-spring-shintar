package ru.otus.hw.shell.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converter.mongo.MongoCommentConverter;
import ru.otus.hw.repository.mongo.MongoCommentRepository;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class MongoCommentCommands {

    private final MongoCommentRepository mongoCommentRepository;

    private final MongoCommentConverter mongoCommentConverter;

    @ShellMethod(value = "Find all comments by book id", key = "macb")
    public String findAllCommentsByBookId(String bookId) {
        return mongoCommentRepository.findAllByBookId(bookId).stream()
                .map(mongoCommentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }
}
