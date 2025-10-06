package ru.otus.hw.rest;

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
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Функциональный эндпоинт для работы с комментариями")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CommentRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    private String anyBookIdWithTitle(String title) {
        var books = webTestClient.get().uri("/api/v2/book")
                .exchange()
                .returnResult(BookDto.class)
                .getResponseBody()
                .collectList()
                .block();
        assertThat(books).isNotNull().isNotEmpty();
        return books.stream()
                .filter(b -> title.equals(b.title()))
                .map(BookDto::id)
                .findFirst()
                .orElseGet(() -> books.get(0).id());
    }

    @DisplayName("должен возвращать список всех комментариев для книги")
    @Test
    void shouldReturnAllCommentsForBook() {
        String bookId = anyBookIdWithTitle("BookTitle_1");

        var comments = webTestClient.get().uri("/api/v2/book/{bookId}/comment", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(CommentDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(comments).isNotNull().hasSize(2);
        assertThat(comments.stream().map(CommentDto::content))
                .containsExactlyInAnyOrder("Comment_1_for_BookTitle_1", "Comment_2_for_BookTitle_1");
        assertThat(comments).allSatisfy(c -> {
            assertThat(c.id()).isNotBlank();
            assertThat(c.content()).isNotBlank();
        });
    }

    @DisplayName("должен создавать комментарий с валидными данными")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateComment() {
        String bookId = anyBookIdWithTitle("BookTitle_1");
        CommentDto requestDto = new CommentDto(null, "Test comment");

        webTestClient.post().uri("/api/v2/book/{bookId}/comment", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestDto), CommentDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommentDto.class)
                .value(created -> {
                    assertThat(created.id()).isNotBlank();
                    assertThat(created.content()).isEqualTo("Test comment");
                });

        var comments = webTestClient.get().uri("/api/v2/book/{bookId}/comment", bookId)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CommentDto.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(comments).isNotNull();
        assertThat(comments.stream().map(CommentDto::content)).contains("Test comment");
    }

    @DisplayName("должен обновлять комментарий с валидными данными")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateComment() {
        String bookId = anyBookIdWithTitle("BookTitle_1");


        List<CommentDto> before = webTestClient.get().uri("/api/v2/book/{bookId}/comment", bookId)
                .exchange()
                .returnResult(CommentDto.class)
                .getResponseBody()
                .collectList()
                .block();
        assertThat(before).isNotNull().isNotEmpty();
        CommentDto toUpdate = before.stream()
                .min(Comparator.comparing(CommentDto::id))
                .orElse(before.get(0));

        CommentDto requestDto = new CommentDto(null, "Updated comment");

        webTestClient.put().uri("/api/v2/book/{bookId}/comment/{commentId}", bookId, toUpdate.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommentDto.class)
                .value(updated -> {
                    assertThat(updated.id()).isEqualTo(toUpdate.id());
                    assertThat(updated.content()).isEqualTo("Updated comment");
                });
    }

    @DisplayName("должен удалять комментарий")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteComment() {
        String bookId = anyBookIdWithTitle("BookTitle_1");


        List<CommentDto> list = webTestClient.get().uri("/api/v2/book/{bookId}/comment", bookId)
                .exchange()
                .returnResult(CommentDto.class)
                .getResponseBody()
                .collectList()
                .block();
        assertThat(list).isNotNull().isNotEmpty();
        String commentId = list.get(0).id();

        webTestClient.delete().uri("/api/v2/book/{bookId}/comment/{commentId}", bookId, commentId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        var after = webTestClient.get().uri("/api/v2/book/{bookId}/comment", bookId)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CommentDto.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(after).isNotNull();
        assertThat(after.stream().map(CommentDto::id)).doesNotContain(commentId);
    }
}
