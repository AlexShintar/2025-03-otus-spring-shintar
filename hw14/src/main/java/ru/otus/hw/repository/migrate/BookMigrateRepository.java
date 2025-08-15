package ru.otus.hw.repository.migrate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.migrate.BookMigrate;

import java.util.Optional;

public interface BookMigrateRepository extends JpaRepository<BookMigrate, String> {
    Optional<BookMigrate> findByMongoId(String mongoId);
}
