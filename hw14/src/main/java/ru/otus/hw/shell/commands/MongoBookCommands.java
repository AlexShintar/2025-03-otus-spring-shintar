package ru.otus.hw.shell.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converter.mongo.MongoBookConverter;
import ru.otus.hw.repository.mongo.MongoBookRepository;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class MongoBookCommands {

    private final MongoBookRepository mongoBookRepository;

    private final MongoBookConverter mongoBookConverter;

    @ShellMethod(value = "Find all mongo books", key = "mab")
    public String findAllBooks() {
        return mongoBookRepository.findAll().stream()
                .map(mongoBookConverter::bookToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }
}
