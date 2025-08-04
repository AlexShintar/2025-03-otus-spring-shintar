package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    Flux<Comment> findAllByBookId(Long bookId);

    Mono<Comment> findByIdAndBookId(long id, long bookId);
}
