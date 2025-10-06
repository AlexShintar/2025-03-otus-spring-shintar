package ru.otus.hw.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.otus.hw.dto.AuthorDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест функционального эндпоинта авторов")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthorRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("должен возвращать список всех авторов")
    void shouldReturnAllAuthors() {
        var expectedNames = List.of("Author_1", "Author_2", "Author_3");

        var authors = webTestClient.get()
                .uri("/api/v2/author")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(authors).isNotNull().hasSize(expectedNames.size());
        assertThat(authors.stream().map(AuthorDto::fullName))
                .containsExactlyInAnyOrderElementsOf(expectedNames);
        assertThat(authors.stream().map(AuthorDto::id))
                .allMatch(id -> id != null && !id.isBlank());
    }
}
