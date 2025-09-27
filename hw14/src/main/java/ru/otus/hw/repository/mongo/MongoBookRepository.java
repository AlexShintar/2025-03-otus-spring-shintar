package ru.otus.hw.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.model.mongo.MongoBook;

public interface MongoBookRepository extends MongoRepository<MongoBook, String> {
}
