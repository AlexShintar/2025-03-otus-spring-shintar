package ru.otus.hw.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.otus.hw.dto.GenreDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Функциональный эндпоинт для работы с жанрами")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GenreRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @DisplayName("должен возвращать список всех жанров")
    @Test
    void shouldReturnAllGenres() {
        var genres = webTestClient.get().uri("/api/v2/genre")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(GenreDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(genres).isNotNull().hasSize(6);
        assertThat(genres.stream().map(GenreDto::name))
                .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
        assertThat(genres.stream().map(GenreDto::id))
                .allMatch(id -> id != null && !id.isBlank());
    }
}
