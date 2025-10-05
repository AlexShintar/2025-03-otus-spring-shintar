package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Flux<Comment> findAllByBookId(String bookId) {
        return commentRepository.findAllByBookId(bookId);
    }

    @Override
    public Mono<Comment> insert(String content, String bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book not found: " + bookId)
                ))
                .flatMap(book -> {
                    Comment comment = new Comment(content, book);
                    return commentRepository.save(comment);
                });
    }

    @Override
    public Mono<Comment> update(String commentId, String bookId, String content) {
        return commentRepository.findById(commentId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Comment not found: " + commentId)
                ))
                .flatMap(existingComment -> {

                    if (!existingComment.getBook().getId().equals(bookId)) {
                        return Mono.error(new EntityNotFoundException(
                                "Comment " + commentId + " not found for book " + bookId
                        ));
                    }

                    existingComment.setContent(content);
                    return commentRepository.save(existingComment);
                });
    }

    @Override
    public Mono<Void> deleteById(String commentId, String bookId) {
        return commentRepository.findById(commentId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Comment not found: " + commentId)
                ))
                .flatMap(comment -> {

                    if (!comment.getBook().getId().equals(bookId)) {
                        return Mono.error(new EntityNotFoundException(
                                "Comment " + commentId + " not found for book " + bookId
                        ));
                    }

                    return commentRepository.deleteById(commentId);
                });
    }
}
