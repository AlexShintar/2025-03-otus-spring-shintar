package ru.otus.hw.actuators;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomHealthIndicator implements HealthIndicator {

    private static final String SQL_PING = "SELECT 1";

    private static final String SQL_H2_VERSION = "SELECT H2VERSION()";

    private static final String SQL_H2_DB_PATH = "CALL DATABASE_PATH()";

    private static final String SQL_H2_MODE =
            "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.INFORMATION_SCHEMA_SETTINGS WHERE SETTING_NAME='MODE'";

    private final JdbcTemplate jdbc;

    private final BookRepository books;

    private final AuthorRepository authors;

    @Override
    public Health health() {
        try {
            if (!dbUp()) {
                return Health.down().withDetail("reason", "DB not reachable").build();
            }
            long b = books.count();
            long a = authors.count();
            Map<String, Object> details = Map.of(
                    "booksCount", b,
                    "authorsCount", a,
                    "h2Version", h2VersionSafe(),
                    "h2Storage", h2StorageSafe(),
                    "h2Mode", h2CompatibilityModeSafe()
            );
            if (b == 0 || a == 0) {
                return Health.outOfService().withDetails(details)
                        .withDetail("reason", "Empty domain data").build();
            }
            return Health.up().withDetails(details).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private boolean dbUp() {
        Integer one = jdbc.queryForObject(SQL_PING, Integer.class);
        return Integer.valueOf(1).equals(one);
    }

    private String h2VersionSafe() {
        try {
            return jdbc.queryForObject(SQL_H2_VERSION, String.class);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String h2StorageSafe() {
        try {
            String path = jdbc.queryForObject(SQL_H2_DB_PATH, String.class);
            return (path == null || path.isBlank()) ? "IN-MEMORY" : "FILE:" + path;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String h2CompatibilityModeSafe() {
        try {
            String mode = jdbc.queryForObject(SQL_H2_MODE, String.class);
            return (mode == null || mode.isBlank()) ? "DEFAULT" : mode;
        } catch (Exception e) {
            return "DEFAULT";
        }
    }
}
