package ru.otus.hw.repository.migrate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.migrate.AuthorMigrate;

import java.util.Optional;

public interface AuthorMigrateRepository extends JpaRepository<AuthorMigrate, String> {
    Optional<AuthorMigrate> findByMongoId(String mongoId);
}
