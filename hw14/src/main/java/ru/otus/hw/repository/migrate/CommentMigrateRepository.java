package ru.otus.hw.repository.migrate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.migrate.CommentMigrate;

import java.util.Optional;

public interface CommentMigrateRepository extends JpaRepository<CommentMigrate, String> {
    Optional<CommentMigrate> findByMongoId(String mongoId);
}
