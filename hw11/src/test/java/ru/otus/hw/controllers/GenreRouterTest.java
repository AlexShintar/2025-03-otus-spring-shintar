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
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.models.Genre;
import ru.otus.hw.rest.GenreRestRouter;
import ru.otus.hw.rest.handlers.GenreHandler;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Функциональный эндпоинт для работы с жанрами")
@WebFluxTest
@Import({GenreRestRouter.class, GenreHandler.class})
@TestPropertySource(properties = "mongock.enabled=false")
class GenreRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private GenreMapper genreMapper;

    @DisplayName("должен возвращать список всех жанров")
    @Test
    void shouldReturnAllGenres() {
        List<Genre> genresFromDb = List.of(
                new Genre("507f1f77bcf86cd799439021", "Genre_1"),
                new Genre("507f1f77bcf86cd799439022", "Genre_2"),
                new Genre("507f1f77bcf86cd799439023", "Genre_3")
        );
        List<GenreDto> expectedDtos = List.of(
                new GenreDto("507f1f77bcf86cd799439021", "Genre_1"),
                new GenreDto("507f1f77bcf86cd799439022", "Genre_2"),
                new GenreDto("507f1f77bcf86cd799439023", "Genre_3")
        );

        when(genreService.findAll()).thenReturn(Flux.fromIterable(genresFromDb));
        when(genreMapper.toDto(any(Genre.class)))
                .thenReturn(expectedDtos.get(0), expectedDtos.get(1), expectedDtos.get(2));

        webTestClient.get().uri("/api/v2/genre")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(GenreDto.class)
                .hasSize(3)
                .isEqualTo(expectedDtos);

        verify(genreService, times(1)).findAll();
        verify(genreMapper, times(3)).toDto(any(Genre.class));
    }
}
