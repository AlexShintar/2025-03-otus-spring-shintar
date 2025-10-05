package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentService {
    Flux<Comment> findAllByBookId(String bookId);

    Mono<Comment> insert(String content, String bookId);

    Mono<Comment> update(String commentId, String bookId, String content);

    Mono<Void> deleteById(String commentId, String bookId);
}
