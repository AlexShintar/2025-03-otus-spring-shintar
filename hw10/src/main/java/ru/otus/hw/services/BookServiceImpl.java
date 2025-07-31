package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookMapper bookMapper;

    private final AuthorMapper authorMapper;

    private final GenreMapper genreMapper;

    @Transactional(readOnly = true)
    @Override
    public BookDto findById(long id) {
        Book book = findByIdOrThrow(bookRepository.findById(id), Book.class, id);
        return bookMapper.toDto(book);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public BookDto insert(BookCreateDto form) {
        Author author = findByIdOrThrow(authorRepository.findById(form.authorId()), Author.class, form.authorId());

        List<Genre> genres = form.genreIds().stream()
                .distinct()
                .map(id -> findByIdOrThrow(genreRepository.findById(id), Genre.class, id))
                .toList();

        Book book = bookMapper.toEntity(
                form,
                authorMapper.toDto(author),
                genres.stream().map(genreMapper::toDto).toList()
        );

        Book saved = bookRepository.save(book);
        return bookMapper.toDto(saved);
    }

    @Transactional
    @Override
    public BookDto update(BookUpdateDto form, long id) {
        Book book = findByIdOrThrow(bookRepository.findById(id), Book.class, id);
        Author author = findByIdOrThrow(authorRepository.findById(form.authorId()), Author.class, form.authorId());

        List<Genre> genres = form.genreIds().stream()
                .distinct()
                .map(gid -> findByIdOrThrow(genreRepository.findById(gid), Genre.class, gid))
                .toList();

        book.setTitle(form.title());
        book.setAuthor(author);
        book.getGenres().clear();
        book.getGenres().addAll(genres);

        Book updated = bookRepository.save(book);
        return bookMapper.toDto(updated);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    private <T> T findByIdOrThrow(Optional<T> optional, Class<?> entityClass, long id) {
        return optional.orElseThrow(() ->
                new EntityNotFoundException(entityClass.getSimpleName() + " not found: " + id));
    }
}
