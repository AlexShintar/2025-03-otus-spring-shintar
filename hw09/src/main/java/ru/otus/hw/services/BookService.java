package ru.otus.hw.services;

import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookDto;

import java.util.List;

public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    @Transactional
    BookDto insert(BookDto bookDto);

    @Transactional
    BookDto update(BookDto bookDto);

    void deleteById(long id);
}
