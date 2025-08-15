package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.migrate.BookMigrate;
import ru.otus.hw.model.mongo.MongoBook;
import ru.otus.hw.model.relation.Author;
import ru.otus.hw.model.relation.Book;
import ru.otus.hw.model.relation.Genre;
import ru.otus.hw.repository.migrate.BookMigrateRepository;
import ru.otus.hw.repository.relation.BookRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookMigrateService {

    private final BookRepository bookRepository;

    private final BookMigrateRepository bookMigrateRepository;

    private final AuthorMigrateService authorMigrateService;

    private final GenreMigrateService genreMigrateService;

    @Cacheable(value = "bookCache", key = "#mongoBook.id")
    public Book save(MongoBook mongoBook) {
        return bookMigrateRepository.findByMongoId(mongoBook.getId())
                .map(m -> bookRepository.findById(m.getBookId()).orElseThrow())
                .orElseGet(() -> {
                    Author author = authorMigrateService.save(mongoBook.getAuthor());
                    List<Genre> genres = mongoBook.getGenres().stream()
                            .map(genreMigrateService::save)
                            .collect(Collectors.toList());
                    Book book = new Book(null, mongoBook.getTitle(), author, genres);
                    book = bookRepository.save(book);
                    bookMigrateRepository.save(new BookMigrate(mongoBook.getId(), book.getId()));
                    System.out.println("Saving book: " + mongoBook.getTitle());
                    System.out.println(book.getTitle() + book.getId() + book.getAuthor());
                    return book;
                });
    }
}
