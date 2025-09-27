package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.migrate.CommentMigrate;
import ru.otus.hw.model.mongo.MongoComment;
import ru.otus.hw.model.relation.Book;
import ru.otus.hw.model.relation.Comment;
import ru.otus.hw.repository.migrate.CommentMigrateRepository;
import ru.otus.hw.repository.relation.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentMigrateService {

    private final CommentRepository commentRepository;

    private final CommentMigrateRepository commentMigrateRepository;

    private final BookMigrateService bookMigrateService;

    public Comment save(MongoComment mongoComment) {
        return commentMigrateRepository.findByMongoId(mongoComment.getId())
                .map(m -> commentRepository.findById(m.getCommentId()).orElseThrow())
                .orElseGet(() -> {
                    Book book = bookMigrateService.save(mongoComment.getBook());
                    Comment comment = new Comment(null, mongoComment.getContent(), book);
                    comment = commentRepository.save(comment);
                    commentMigrateRepository.save(new CommentMigrate(mongoComment.getId(), comment.getId()));

                    System.out.println(comment);
                    return comment;
                });
    }
}
