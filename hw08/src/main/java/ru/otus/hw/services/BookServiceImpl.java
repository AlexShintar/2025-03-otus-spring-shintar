package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookConverter bookConverter;

    @Transactional(readOnly = true)
    @Override
    public Optional<BookDto> findById(long id) {
        return bookRepository.findById(id)
                .map(bookConverter::toDto);
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
    public BookDto insert(String title, long authorId, Set<Long> genreIds) {
        Author author = fetchAuthor(authorId);
        List<Genre> genres = fetchGenres(genreIds);
        Book book = new Book(0, title, author, genres);
        Book saved = bookRepository.save(book);
        return bookConverter.toDto(saved);
    }

    @Transactional
    @Override
    public BookDto update(long id, String title, long authorId, Set<Long> genreIds) {
        Author author = fetchAuthor(authorId);
        List<Genre> genres = fetchGenres(genreIds);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %d not found", id)
                ));
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenres(genres);
        Book updated = bookRepository.save(book);
        return bookConverter.toDto(updated);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    private Author fetchAuthor(long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Author with id %d not found".formatted(authorId))
                );
    }

    private List<Genre> fetchGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            throw new IllegalArgumentException("Genres ids must not be null or empty");
        }
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new EntityNotFoundException("One or more genres with ids %s not found".formatted(genreIds));
        }
        return genres;
    }
}
