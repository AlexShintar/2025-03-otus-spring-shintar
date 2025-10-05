package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест книг")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BookRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("должен возвращать список всех книг")
    void shouldReturnAllBooksFromSeeds() {
        var books = webTestClient.get().uri("/api/v2/book")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(books).isNotNull().hasSize(3);
        assertThat(books.stream().map(BookDto::title))
                .containsExactlyInAnyOrder("BookTitle_1", "BookTitle_2", "BookTitle_3");
    }

    @Test
    @DisplayName("должен возвращать книгу по id")
    void shouldReturnBookById() {
        var list = webTestClient.get().uri("/api/v2/book")
                .exchange()
                .returnResult(BookDto.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(list).isNotNull().isNotEmpty();
        var first = list.get(0);

        webTestClient.get().uri("/api/v2/book/{id}", first.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .value(b -> {
                    assertThat(b.id()).isEqualTo(first.id());
                    assertThat(b.title()).isNotBlank();
                });
    }

    @DisplayName("должен возвращать 404 Not Found, если книга по id не найдена")
    @Test
    void shouldReturnNotFoundForNonExistentBook() {
        String nonExistentId = "ffffffffffffffffffffffff";

        webTestClient.get().uri("/api/v2/book/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> {
                });
    }

    @Test
    @DisplayName("должен создавать книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateBook() {
        var authors = webTestClient.get().uri("/api/v2/author")
                .exchange()
                .returnResult(AuthorDto.class)
                .getResponseBody()
                .collectList()
                .block();
        var genres = webTestClient.get().uri("/api/v2/genre")
                .exchange()
                .returnResult(GenreDto.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(authors).isNotNull().isNotEmpty();
        assertThat(genres).isNotNull().isNotEmpty();

        var author = authors.get(0);
        var genre = genres.get(0);

        var payload = new BookCreateDto("Test Book", author.id(), List.of(genre.id()));

        webTestClient.post().uri("/api/v2/book")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(payload), BookCreateDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(BookDto.class)
                .value(b -> {
                    assertThat(b.id()).isNotBlank();
                    assertThat(b.title()).isEqualTo("Test Book");
                    assertThat(b.author().id()).isEqualTo(author.id());
                    assertThat(b.genres()).extracting(GenreDto::id).contains(genre.id());
                });
    }

    @Test
    @DisplayName("должен обновлять книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateBook() {
        var books = webTestClient.get().uri("/api/v2/book")
                .exchange()
                .returnResult(BookDto.class)
                .getResponseBody()
                .collectList()
                .block();
        assertThat(books).isNotNull().isNotEmpty();
        var book = books.get(0);

        var authors = webTestClient.get().uri("/api/v2/author")
                .exchange()
                .returnResult(AuthorDto.class)
                .getResponseBody()
                .collectList()
                .block();
        var genres = webTestClient.get().uri("/api/v2/genre")
                .exchange()
                .returnResult(GenreDto.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(authors).isNotNull().hasSizeGreaterThanOrEqualTo(1);
        assertThat(genres).isNotNull().isNotEmpty();

        var newAuthor = authors.stream()
                .filter(a -> !a.id().equals(book.author().id()))
                .findFirst()
                .orElse(authors.get(0));
        var newGenre = genres.get(0);

        var payload = new BookUpdateDto("Updated Book", newAuthor.id(), List.of(newGenre.id()));

        webTestClient.put().uri("/api/v2/book/{id}", book.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .value(updated -> {
                    assertThat(updated.id()).isEqualTo(book.id());
                    assertThat(updated.title()).isEqualTo("Updated Book");
                    assertThat(updated.author().id()).isEqualTo(newAuthor.id());
                    assertThat(updated.genres()).extracting(GenreDto::id).contains(newGenre.id());
                });
    }

    @Test
    @DisplayName("должен удалять книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteBook() {
        var first = webTestClient.get().uri("/api/v2/book")
                .exchange()
                .returnResult(BookDto.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(first).isNotNull().isNotEmpty();
        var bookId = first.get(0).id();

        webTestClient.delete().uri("/api/v2/book/{id}", bookId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webTestClient.get().uri("/api/v2/book/{id}", bookId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
