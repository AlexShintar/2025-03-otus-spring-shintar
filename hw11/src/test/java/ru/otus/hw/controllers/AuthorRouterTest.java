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
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.rest.AuthorRestRouter;
import ru.otus.hw.rest.handlers.AuthorHandler;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Функциональный эндпоинт для работы с авторами")
@WebFluxTest
@Import({AuthorRestRouter.class, AuthorHandler.class})
@TestPropertySource(properties = "mongock.enabled=false")
class AuthorRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private AuthorMapper authorMapper;

    @DisplayName("должен возвращать список всех авторов")
    @Test
    void shouldReturnAllAuthors() {
        List<Author> authorsFromDb = List.of(
                new Author("507f1f77bcf86cd799439011", "Author_1"),
                new Author("507f1f77bcf86cd799439012", "Author_2")
        );
        List<AuthorDto> authorDtos = List.of(
                new AuthorDto("507f1f77bcf86cd799439011", "Author_1"),
                new AuthorDto("507f1f77bcf86cd799439012", "Author_2")
        );

        when(authorService.findAll()).thenReturn(Flux.fromIterable(authorsFromDb));
        when(authorMapper.toDto(any(Author.class)))
                .thenReturn(authorDtos.get(0), authorDtos.get(1));

        webTestClient.get().uri("/api/v2/author")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(2)
                .isEqualTo(authorDtos);

        verify(authorService, times(1)).findAll();
        verify(authorMapper, times(2)).toDto(any(Author.class));
    }
}
