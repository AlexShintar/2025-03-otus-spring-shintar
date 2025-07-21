package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final CommentConverter commentConverter;

    @Transactional(readOnly = true)
    @Override
    public Optional<CommentDto> findById(long id) {
        return commentRepository.findById(id)
                .map(commentConverter::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> findAllByBookId(long bookId) {
        List<Comment> comments = commentRepository.findAllByBookId(bookId);
        return comments.stream()
                .map(commentConverter::toDto)
                .toList();
    }

    @Transactional
    @Override
    public CommentDto insert(String content, long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        Comment comment = new Comment(null, content, book);
        Comment saved = commentRepository.save(comment);
        return commentConverter.toDto(saved);
    }

    @Transactional
    @Override
    public CommentDto update(long id, long bookId, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        comment.setContent(content);

        if (!comment.getBook().getId().equals(bookId)) {
            throw new IllegalArgumentException("Comment with id %d does not belong to book with id %d"
                    .formatted(id, bookId));
        }

        Comment saved = commentRepository.save(comment);
        return commentConverter.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteById(long id, long bookId) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            return;
        }
        Comment comment = commentOpt.get();
        if (!comment.getBook().getId().equals(bookId)) {
            throw new IllegalArgumentException("Comment with id %d does not belong to book with id %d"
                    .formatted(id, bookId));
        }
        commentRepository.deleteById(id);
    }
}
