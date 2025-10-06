package ru.otus.hw.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@DisplayName("Интеграционный тест ошибок роутов книг")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BookRouterErrorsTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("должен возвращать 404 Not Found при создании книги, если не найден автор")
    void shouldReturnNotFoundOnCreateWhenAuthorMissing() {
        Mockito.when(bookService.insert(any(BookCreateDto.class)))
                .thenReturn(Mono.error(new EntityNotFoundException("Author not found")));

        var payload = new BookCreateDto("Valid Title", "bad-author-id", List.of("g1"));

        webTestClient.post().uri("/api/v2/book")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_HTML)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("<!DOCTYPE html>");
                    assertThat(html).contains("Author not found");
                });
    }

    @Test
    @DisplayName("должен возвращать 404 Not Found при обновлении книги, если не найден жанр")
    void shouldReturnNotFoundOnUpdateWhenGenreMissing() {
        Mockito.when(bookService.update(any(BookUpdateDto.class), anyString()))
                .thenReturn(Mono.error(new EntityNotFoundException("Genre not found")));

        var payload = new BookUpdateDto("Valid Title", "a1", List.of("bad-genre"));

        webTestClient.put().uri("/api/v2/book/{id}", "some-id")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_HTML)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("<!DOCTYPE html>");
                    assertThat(html).contains("Genre not found");
                });
    }

    @Test
    @DisplayName("должен возвращать 400 Bad Request при ошибках валидации тела запроса")
    void shouldReturnBadRequestOnValidationErrors() {
        var invalid = new BookCreateDto("", "", List.of());

        webTestClient.post().uri("/api/v2/book")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_HTML)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("<!DOCTYPE html>");
                    assertThat(html).contains("Please provide a title");
                    assertThat(html).contains("Title must be between 2 and 255 characters");
                    assertThat(html).contains("Please select at least one genre");
                });

        Mockito.verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("должен возвращать 500 Internal Server Error при ошибке сервиса/репозитория")
    void shouldReturnInternalServerErrorOnRepositoryFailure() {
        Mockito.when(bookService.findAll())
                .thenReturn(Flux.error(new RuntimeException("DB down")));

        webTestClient.get().uri("/api/v2/book")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("<!DOCTYPE html>");
                    assertThat(html).contains("DB down");
                });
    }
}
