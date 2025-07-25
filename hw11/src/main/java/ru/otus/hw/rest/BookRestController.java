package ru.otus.hw.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    private final BookConverter bookConverter;

    @GetMapping("/api/v1/book")
    public ResponseEntity<List<BookDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/api/v1/book/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PostMapping("/api/v1/book")
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookCreateDto bookCreateDto) {
        var created = bookService.insert(bookConverter.fromFormDto(bookCreateDto));
        return ResponseEntity.created(URI.create("/api/books/" + created.getId()))
                .body(created);
    }

    @PutMapping("/api/v1/book/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable long id,
                                              @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        var updated = bookService.update(bookConverter.fromFormDto(bookUpdateDto, id));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/v1/book/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
