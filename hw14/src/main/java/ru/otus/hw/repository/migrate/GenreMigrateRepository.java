package ru.otus.hw.repository.migrate;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.migrate.GenreMigrate;

import java.util.Optional;

public interface GenreMigrateRepository extends JpaRepository<GenreMigrate, String> {
    Optional<GenreMigrate> findByMongoId(String mongoId);
}
