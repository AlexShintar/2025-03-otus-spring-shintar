package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Override
    public Flux<Comment> findAllByBookId(long bookId) {
        return commentRepository.findAllByBookId(bookId);
    }

    @Override
    public Mono<Comment> insert(String content, long bookId) {
        Comment comment = new Comment(null, content, bookId);
        return commentRepository.save(comment);
    }

    @Override
    public Mono<Comment> update(long commentId, long bookId, String content) {
        return commentRepository.findByIdAndBookId(commentId, bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Comment not found: " + commentId)))
                .map(existingComment -> new Comment(
                        existingComment.id(),
                        content,
                        existingComment.bookId()
                ))
                .flatMap(commentRepository::save);
    }

    @Override
    public Mono<Void> deleteById(long commentId, long bookId) {
        return commentRepository.findByIdAndBookId(commentId, bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Comment not found: " + commentId)))
                .flatMap(comment -> commentRepository.deleteById(commentId));
    }
}
