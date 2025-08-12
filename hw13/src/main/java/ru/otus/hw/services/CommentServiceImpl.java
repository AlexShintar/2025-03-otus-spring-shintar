package ru.otus.hw.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;
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

@Service
public class CommentServiceImpl implements CommentService {

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final CommentConverter commentConverter;

    private final AclServiceWrapperService aclServiceWrapperService;

    private final CommentService self;

    public CommentServiceImpl(BookRepository bookRepository, CommentRepository commentRepository,
                              CommentConverter commentConverter, AclServiceWrapperService aclServiceWrapperService,
                              @Lazy CommentService self) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
        this.commentConverter = commentConverter;
        this.aclServiceWrapperService = aclServiceWrapperService;
        this.self = self;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CommentDto> findById(long id) {
        return commentRepository.findById(id)
                .map(commentConverter::toDto);
    }

    @Override
    public List<CommentDto> findAllByBookId(long bookId) {
        List<Comment> allowed = self.findAllByBookIdWithAcl(bookId);
        return allowed.stream()
                .map(commentConverter::toDto)
                .toList();
    }

    @Override
    @PostFilter("hasRole('ADMIN') or hasPermission(filterObject.id, 'ru.otus.hw.models.Comment', 'READ')")
    public List<Comment> findAllByBookIdWithAcl(long bookId) {
        return commentRepository.findAllByBookId(bookId);
    }

    @Transactional
    @Override
    public CommentDto insert(String content, long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        Comment comment = new Comment(null, content, book);
        Comment saved = commentRepository.save(comment);
        aclServiceWrapperService.createAcl(saved);
        Sid userSid = new GrantedAuthoritySid("ROLE_USER");
        aclServiceWrapperService.addPermission(saved, userSid, BasePermission.READ);
        return commentConverter.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'ru.otus.hw.models.Comment', 'WRITE')")
    @Transactional
    @Override
    public CommentDto update(long id, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);
        return commentConverter.toDto(saved);
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'ru.otus.hw.models.Comment', 'DELETE')")
    @Transactional
    @Override
    public void deleteById(long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        aclServiceWrapperService.deleteAcl(comment);
        commentRepository.delete(comment);
    }
}
