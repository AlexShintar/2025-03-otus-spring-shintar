package ru.otus.hw.converter.relation;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.relation.Author;

@Component
public class AuthorConverter {
    public String authorToString(Author author) {
        return "Id: %d, FullName: %s".formatted(author.getId(), author.getFullName());
    }
}
