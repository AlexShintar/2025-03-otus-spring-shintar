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
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест GlobalExceptionHandler")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("должен отдавать HTML-шаблон ошибки с детальными сообщениями валидации")
    void shouldRenderHtmlWithDetailedValidationMessages() {
        var invalid = new BookCreateDto("", "", List.of());

        webTestClient.post()
                .uri("/api/v2/book")
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
    @DisplayName("должен отдавать HTML-шаблон ошибки при внутреннем Exception (500)")
    void shouldRenderHtmlOnInternalServerError() {
        Mockito.when(bookService.findAll())
                .thenReturn(Flux.error(new RuntimeException("DB down")));

        webTestClient.get()
                .uri("/api/v2/book")
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
