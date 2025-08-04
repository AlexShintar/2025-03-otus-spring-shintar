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
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookGenreRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookGenreRepository bookGenreRepository;

    private final BookMapper bookMapper;

    private final AuthorMapper authorMapper;

    private final GenreMapper genreMapper;

    @Override
    public Mono<BookDto> findById(long id) {
        return bookRepository.findBookDtoById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book not found: " + id)));
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllDto();
    }

    @Override
    public Mono<BookDto> insert(BookCreateDto form) {
        return fetchAuthorAndGenres(form.authorId(), form.genreIds())
                .flatMap(authorAndGenres -> {
                    Author author = authorAndGenres.getT1();
                    List<Genre> genres = authorAndGenres.getT2();
                    Book bookToSave = new Book(null, form.title(), author.id());

                    return bookRepository.save(bookToSave)
                            .flatMap(savedBook -> linkGenresAndBuildDto(savedBook, author, genres));
                });
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto form, long id) {
        Mono<Book> bookMono = bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book not found: " + id)));
        Mono<Tuple2<Author, List<Genre>>> authorAndGenresMono = fetchAuthorAndGenres(form.authorId(), form.genreIds());

        return Mono.zip(bookMono, authorAndGenresMono)
                .flatMap(data -> {
                    Author author = data.getT2().getT1();
                    List<Genre> genres = data.getT2().getT2();
                    var updatedBookEntity = new Book(id, form.title(), author.id());

                    return bookGenreRepository.deleteLinksByBookId(id)
                            .then(bookRepository.save(updatedBookEntity))
                            .flatMap(savedBook -> linkGenresAndBuildDto(savedBook, author, genres));
                });
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book not found: " + id)))
                .flatMap(existingBook -> bookRepository.deleteById(existingBook.id()));
    }

    private Mono<Tuple2<Author, List<Genre>>> fetchAuthorAndGenres(long authorId, List<Long> genreIds) {
        Mono<Author> authorMono = authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Author not found: " + authorId)));
        Mono<List<Genre>> genresMono = Flux.fromIterable(genreIds)
                .distinct()
                .flatMap(id -> genreRepository.findById(id)
                        .switchIfEmpty(Mono.error(new EntityNotFoundException("Genre not found: " + id))))
                .collectList();
        return Mono.zip(authorMono, genresMono);
    }

    private Mono<BookDto> linkGenresAndBuildDto(Book savedBook, Author author, List<Genre> genres) {
        return bookGenreRepository.linkGenresToBook(
                savedBook.id(),
                genres.stream().map(Genre::id).toList()
        ).then(Mono.fromCallable(() -> bookMapper.toDto(
                savedBook,
                authorMapper.toDto(author),
                genres.stream().map(genreMapper::toDto).toList()
        )));
    }
}
