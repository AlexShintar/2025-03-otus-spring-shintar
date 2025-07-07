package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

@RequiredArgsConstructor
@Component
public class AuthorConverter {

    public AuthorDto toDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }

    public Author toEntity(AuthorDto dto) {
        return new Author(dto.getId(), dto.getFullName());
    }
}
