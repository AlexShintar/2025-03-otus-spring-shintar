package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final AuthorConverter authorConverter;

    @Transactional(readOnly = true)
    @Override
    public List<AuthorDto> findAll() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream()
                .map(authorConverter::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Author findById(long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found: " + id));
    }
}