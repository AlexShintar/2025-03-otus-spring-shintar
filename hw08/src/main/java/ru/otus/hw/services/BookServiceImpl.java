package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final BookConverter bookConverter;

    @Override
    public Optional<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .map(bookConverter::toDto);
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookConverter::toDto)
                .toList();
    }

    @Override
    public BookDto insert(String title, String authorId, Set<String> genreIds) {
        Author author = fetchAuthor(authorId);
        Set<Genre> genres = fetchGenres(genreIds);
        Book book = new Book(title, author, genres);
        Book saved = bookRepository.save(book);
        return bookConverter.toDto(saved);
    }

    @Override
    public BookDto update(String id, String title, String authorId, Set<String> genreIds) {
        Author author = fetchAuthor(authorId);
        Set<Genre> genres = fetchGenres(genreIds);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %s not found", id)
                ));
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenres(genres);
        Book updated = bookRepository.save(book);
        return bookConverter.toDto(updated);
    }

    @Override
    public void deleteById(String id) {
        bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %s not found", id)
                ));
        commentRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
    }

    private Author fetchAuthor(String authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Author with iв %s not found".formatted(authorId))
                );
    }

    private Set<Genre> fetchGenres(Set<String> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            throw new IllegalArgumentException("Genres ids must not be null or empty");
        }
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new EntityNotFoundException("One or more genres with ids %s not found".formatted(genreIds));
        }
        return new HashSet<>(genres);
    }
}
