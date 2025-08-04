package ru.otus.hw.services;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentService {
    Flux<Comment> findAllByBookId(long bookId);

    Mono<Comment> insert(String content, long bookId);

    Mono<Comment> update(long commentId, long bookId, String content);

    Mono<Void> deleteById(long commentId, long bookId);
}
