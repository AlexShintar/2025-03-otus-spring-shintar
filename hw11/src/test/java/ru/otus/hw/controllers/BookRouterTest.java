package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.rest.BookRestRouter;
import ru.otus.hw.rest.handlers.BookHandler;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Функциональный эндпоинт для работы с книгами")
@WebFluxTest
@Import({BookRestRouter.class, BookHandler.class})
@TestPropertySource(properties = "mongock.enabled=false")
class BookRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookMapper bookMapper;

    private final AuthorDto author = new AuthorDto("507f1f77bcf86cd799439011", "Author_1");
    private final List<GenreDto> genres = List.of(
            new GenreDto("507f1f77bcf86cd799439021", "Genre_1")
    );

    @DisplayName("должен возвращать список всех книг")
    @Test
    void shouldReturnAllBooks() {
        var books = List.of(
                new BookDto("507f1f77bcf86cd799439031", "BookTitle_1", author, genres),
                new BookDto("507f1f77bcf86cd799439032", "BookTitle_2", author, genres)
        );
        when(bookService.findAll()).thenReturn(Flux.fromIterable(books));

        webTestClient.get().uri("/api/v2/book")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(BookDto.class)
                .isEqualTo(books);
        verify(bookService).findAll();
    }

    @DisplayName("должен возвращать книгу по id")
    @Test
    void shouldReturnBookById() {
        String bookId = "507f1f77bcf86cd799439031";
        var book = new BookDto(bookId, "Test Book", author, genres);
        when(bookService.findById(bookId)).thenReturn(Mono.just(book));

        webTestClient.get().uri("/api/v2/book/{id}", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .isEqualTo(book);
        verify(bookService).findById(bookId);
    }

    @DisplayName("должен возвращать 404 Not Found, если книга по id не найдена")
    @Test
    void shouldReturnNotFoundForNonExistentBook() {
        String nonExistentId = "507f1f77bcf86cd799439099";
        when(bookService.findById(nonExistentId)).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v2/book/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
        verify(bookService).findById(nonExistentId);
    }

    @DisplayName("должен создавать книгу")
    @Test
    void shouldCreateBook() {
        String createdBookId = "507f1f77bcf86cd799439031";
        String authorId = "507f1f77bcf86cd799439011";
        String genreId = "507f1f77bcf86cd799439021";

        var createDto = new BookCreateDto("Test Book", authorId, List.of(genreId));
        var expectedDto = new BookDto(createdBookId, "Test Book", author, genres);

        when(bookService.insert(any(BookCreateDto.class))).thenReturn(Mono.just(expectedDto));

        webTestClient.post().uri("/api/v2/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/v2/book/" + createdBookId)
                .expectBody(BookDto.class)
                .isEqualTo(expectedDto);
        verify(bookService).insert(any(BookCreateDto.class));
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        String bookId = "507f1f77bcf86cd799439031";
        String authorId = "507f1f77bcf86cd799439011";
        String genreId = "507f1f77bcf86cd799439021";

        var updateDto = new BookUpdateDto("Updated Book", authorId, List.of(genreId));
        var expectedDto = new BookDto(bookId, "Updated Book", author, genres);

        when(bookService.update(eq(updateDto), eq(bookId))).thenReturn(Mono.just(expectedDto));

        webTestClient.put().uri("/api/v2/book/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .isEqualTo(expectedDto);
        verify(bookService).update(eq(updateDto), eq(bookId));
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() {
        String bookId = "507f1f77bcf86cd799439031";
        when(bookService.deleteById(bookId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v2/book/{id}", bookId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(bookService).deleteById(bookId);
    }
}
