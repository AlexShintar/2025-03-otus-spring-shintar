package ru.otus.hw.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.model.mongo.MongoComment;

import java.util.List;

public interface MongoCommentRepository extends MongoRepository<MongoComment, String> {
    List<MongoComment> findAllByBookId(String bookId);
}
