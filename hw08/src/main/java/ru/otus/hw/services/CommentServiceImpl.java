package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Override
    public Optional<CommentDto> findById(String id) {
        return commentRepository.findById(id)
                .map(commentConverter::toDto);
    }

    @Override
    public List<CommentDto> findAllByBookId(String bookId) {
        List<Comment> comments = commentRepository.findAllByBookId(bookId);
        return comments.stream()
                .map(commentConverter::toDto)
                .toList();
    }

    @Override
    public CommentDto insert(String content, String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        Comment comment = new Comment(content, book);
        Comment saved = commentRepository.save(comment);
        return commentConverter.toDto(saved);
    }

    @Override
    public CommentDto update(String id, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);
        return commentConverter.toDto(saved);
    }

    @Override
    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }
}