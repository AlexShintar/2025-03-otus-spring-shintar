package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.migrate.AuthorMigrate;
import ru.otus.hw.model.mongo.MongoAuthor;
import ru.otus.hw.model.relation.Author;
import ru.otus.hw.repository.migrate.AuthorMigrateRepository;
import ru.otus.hw.repository.relation.AuthorRepository;

@RequiredArgsConstructor
@Service
public class AuthorMigrateService {

    private final AuthorRepository authorRepository;

    private final AuthorMigrateRepository authorMigrateRepository;

    @Cacheable(value = "authorCache", key = "#mongoAuthor.id")
    public Author save(MongoAuthor mongoAuthor) {
        return authorMigrateRepository.findByMongoId(mongoAuthor.getId())
                .map(migrate -> authorRepository.findById(migrate.getAuthorId()).orElseThrow())
                .orElseGet(() -> {
                    Author author = new Author(null, mongoAuthor.getFullName());
                    author = authorRepository.save(author);
                    authorMigrateRepository.save(new AuthorMigrate(mongoAuthor.getId(), author.getId()));
                    return author;
                });
    }
}
