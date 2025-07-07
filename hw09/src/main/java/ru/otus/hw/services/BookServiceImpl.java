package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    private final BookConverter bookConverter;

    @Transactional(readOnly = true)
    @Override
    public BookDto findById(long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %d not found", id)
                ));
        return bookConverter.toDto(book);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookConverter::toDto)
                .toList();
    }

    @Transactional
    @Override
    public BookDto insert(BookDto bookDto) {
        Book book = bookConverter.toEntity(bookDto);
        Book saved = bookRepository.save(book);
        return bookConverter.toDto(saved);
    }

    @Transactional
    @Override
    public BookDto update(BookDto bookDto) {
        Book book = bookRepository.findById(bookDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %d not found", bookDto.getId())
                ));
        Book updated = bookRepository.save(bookConverter.updateEntity(book, bookDto));
        return bookConverter.toDto(updated);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }
}
