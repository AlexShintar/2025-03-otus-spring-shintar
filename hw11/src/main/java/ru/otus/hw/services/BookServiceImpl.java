package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookMapper bookMapper;

    @Override
    public Mono<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDto)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book not found: " + id)));
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .map(bookMapper::toDto);
    }

    @Override
    public Mono<BookDto> insert(BookCreateDto form) {
        return fetchAuthorAndGenres(form.authorId(), form.genreIds())
                .flatMap(tuple -> {
                    Author author = tuple.getT1();
                    Set<Genre> genres = tuple.getT2();


                    Book book = new Book(form.title(), author, genres);

                    return bookRepository.save(book)
                            .map(bookMapper::toDto);
                });
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto form, String id) {
        Mono<Book> bookMono = bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book not found: " + id)));

        Mono<Tuple2<Author, Set<Genre>>> dataMono = fetchAuthorAndGenres(
                form.authorId(),
                form.genreIds()
        );

        return Mono.zip(bookMono, dataMono)
                .flatMap(tuple -> {
                    Book existingBook = tuple.getT1();
                    Author author = tuple.getT2().getT1();
                    Set<Genre> genres = tuple.getT2().getT2();


                    existingBook.setTitle(form.title());
                    existingBook.setAuthor(author);
                    existingBook.setGenres(genres);

                    return bookRepository.save(existingBook)
                            .map(bookMapper::toDto);
                });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book not found: " + id)))
                .flatMap(book -> bookRepository.deleteById(book.getId()));
    }

    private Mono<Tuple2<Author, Set<Genre>>> fetchAuthorAndGenres(
            String authorId,
            List<String> genreIds) {

        Mono<Author> authorMono = authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Author not found: " + authorId)
                ));

        Mono<Set<Genre>> genresMono = Flux.fromIterable(genreIds)
                .distinct()
                .flatMap(id -> genreRepository.findById(id)
                        .switchIfEmpty(Mono.error(
                                new EntityNotFoundException("Genre not found: " + id)
                        )))
                .collect(Collectors.toSet());

        return Mono.zip(authorMono, genresMono);
    }
}
