package ru.otus.hw.services;

import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;

import java.util.List;

public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    @Transactional
    BookDto insert(BookCreateDto bookCreateDto);

    @Transactional
    BookDto update(BookUpdateDto bookUpdateDto, long id);

    void deleteById(long id);
}
