package ru.otus.hw.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AclAwareBookReader {
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Book> findAllBooksSecured() {
        return bookRepository.findAll();
    }
}