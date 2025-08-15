package ru.otus.hw.repository.relation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.otus.hw.model.relation.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @NonNull
    List<Comment> findAllByBookId(long bookId);
}