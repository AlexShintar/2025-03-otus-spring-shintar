package ru.otus.hw.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookRecommendationDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.ExternalBookRecommendationService;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    private final ExternalBookRecommendationService recommendationService;

    @GetMapping("/api/v1/book")
    public List<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    /**
     * При вызове дополнительно запрашивает рекомендацию у внешнего сервиса (CB/Retry/TimeLimiter).
     * Можно обогатить BookDto рекомендацией.
     */
    @GetMapping("/api/v1/book/{id}")
    public BookDto getBookById(@PathVariable long id) {
        BookDto book = bookService.findById(id);

        CompletableFuture<BookRecommendationDto> recommendationFuture =
                recommendationService.getRecommendation(id);

        recommendationFuture.whenComplete((recommendation, ex) -> {
            if (ex != null) {
                log.error("Failed to get recommendation: {}", ex.getMessage());
            } else {
                log.info("Got recommendation: {}", recommendation);
            }
        });
        // Рекомендация только логируется.
        // При наличии соответствующего поля в BookDto можно его присваивать.
        return book;
    }

    @PostMapping("/api/v1/book")
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookCreateDto bookCreateDto) {
        var created = bookService.insert(bookCreateDto);
        return ResponseEntity.created(URI.create("/api/v1/book/" + created.id()))
                .body(created);
    }

    @PutMapping("/api/v1/book/{id}")
    public BookDto updateBook(@PathVariable long id,
                              @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        return bookService.update(bookUpdateDto, id);
    }

    @DeleteMapping("/api/v1/book/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable long id) {
        bookService.deleteById(id);
    }
}
