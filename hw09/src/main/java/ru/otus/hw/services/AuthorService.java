package ru.otus.hw.services;

import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> findAll();

    @Transactional(readOnly = true)
    Author findById(long id);
}
