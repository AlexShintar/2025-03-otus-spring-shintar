package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.models.Comment;
import ru.otus.hw.rest.CommentRestRouter;
import ru.otus.hw.rest.handlers.CommentHandler;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Функциональный эндпоинт для работы с комментариями")
@WebFluxTest
@Import({CommentRestRouter.class, CommentHandler.class})
class CommentRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private CommentMapper commentMapper;

    @DisplayName("должен возвращать список всех комментариев для книги")
    @Test
    void shouldReturnAllCommentsForBook() {
        long bookId = 1L;
        List<Comment> commentEntities = List.of(new Comment(1L, "Comment_1", bookId), new Comment(2L, "Comment_2", bookId));
        List<CommentDto> expectedDtos = List.of(new CommentDto(1L, "Comment_1"), new CommentDto(2L, "Comment_2"));

        when(commentService.findAllByBookId(bookId)).thenReturn(Flux.fromIterable(commentEntities));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(expectedDtos.get(0), expectedDtos.get(1));

        webTestClient.get().uri("/api/v2/book/{bookId}/comment", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(CommentDto.class)
                .isEqualTo(expectedDtos);

        verify(commentService).findAllByBookId(bookId);
        verify(commentMapper, times(2)).toDto(any(Comment.class));
    }

    @DisplayName("должен создавать комментарий с валидными данными")
    @Test
    void shouldCreateComment() {
        long bookId = 1L;
        CommentDto requestDto = new CommentDto(null, "Test comment");
        Comment savedEntity = new Comment(1L, "Test comment", bookId);
        CommentDto expectedDto = new CommentDto(1L, "Test comment");

        when(commentService.insert(eq("Test comment"), eq(bookId))).thenReturn(Mono.just(savedEntity));
        when(commentMapper.toDto(savedEntity)).thenReturn(expectedDto);

        webTestClient.post().uri("/api/v2/book/{bookId}/comment", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommentDto.class)
                .isEqualTo(expectedDto);

        verify(commentService).insert("Test comment", bookId);
        verify(commentMapper).toDto(savedEntity);
    }

    @DisplayName("должен обновлять комментарий с валидными данными")
    @Test
    void shouldUpdateComment() {
        long bookId = 1L;
        long commentId = 1L;
        CommentDto requestDto = new CommentDto(null, "Updated comment");
        Comment updatedEntity = new Comment(commentId, "Updated comment", bookId);
        CommentDto expectedDto = new CommentDto(commentId, "Updated comment");

        when(commentService.update(eq(commentId), eq(bookId), eq("Updated comment"))).thenReturn(Mono.just(updatedEntity));
        when(commentMapper.toDto(updatedEntity)).thenReturn(expectedDto);

        webTestClient.put().uri("/api/v2/book/{bookId}/comment/{commentId}", bookId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommentDto.class)
                .isEqualTo(expectedDto);

        verify(commentService).update(commentId, bookId, "Updated comment");
        verify(commentMapper).toDto(updatedEntity);
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteComment() {
        long bookId = 1L;
        long commentId = 1L;
        when(commentService.deleteById(eq(commentId), eq(bookId))).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v2/book/{bookId}/comment/{commentId}", bookId, commentId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(commentService).deleteById(commentId, bookId);
    }
}
