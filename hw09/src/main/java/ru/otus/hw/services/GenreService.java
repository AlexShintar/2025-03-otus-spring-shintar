package ru.otus.hw.services;

import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;

import java.util.List;

public interface GenreService {
    List<GenreDto> findAll();

    Genre findById(long id);
}
