package ru.otus.hw.rest.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.services.CommentService;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class CommentHandler {
    private final CommentService commentService;

    private final CommentMapper commentMapper;

    public Mono<ServerResponse> getComments(ServerRequest request) {
        String bookId = request.pathVariable("bookId"); // String
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(commentService.findAllByBookId(bookId)
                        .map(commentMapper::toDto), CommentDto.class);
    }

    public Mono<ServerResponse> addComment(ServerRequest request) {
        String bookId = request.pathVariable("bookId"); // String
        return request.bodyToMono(CommentDto.class)
                .flatMap(commentDto -> commentService.insert(commentDto.content(), bookId))
                .map(commentMapper::toDto)
                .flatMap(created -> ServerResponse
                        .created(URI.create("/api/v2/book/" + bookId + "/comment/" + created.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(created));
    }

    public Mono<ServerResponse> updateComment(ServerRequest request) {
        String bookId = request.pathVariable("bookId"); // String
        String commentId = request.pathVariable("commentId"); // String
        return request.bodyToMono(CommentDto.class)
                .flatMap(commentDto -> commentService.update(commentId, bookId, commentDto.content()))
                .map(commentMapper::toDto)
                .flatMap(updated -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updated));
    }

    public Mono<ServerResponse> deleteComment(ServerRequest request) {
        String bookId = request.pathVariable("bookId"); // String
        String commentId = request.pathVariable("commentId"); // String
        return commentService.deleteById(commentId, bookId)
                .then(ServerResponse.noContent().build());
    }
}
