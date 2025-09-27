package ru.otus.hw.actuators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomHealthIndicatorTest {

    @Test
    void shouldReturnUp_whenDbOk_andDomainNotEmpty() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BookRepository books = mock(BookRepository.class);
        AuthorRepository authors = mock(AuthorRepository.class);

        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbc.queryForObject("SELECT H2VERSION()", String.class)).thenReturn("2.2.224");
        when(jdbc.queryForObject("SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME='MODE'", String.class))
                .thenReturn("DEFAULT");
        when(books.count()).thenReturn(5L);
        when(authors.count()).thenReturn(3L);

        var indicator = new CustomHealthIndicator(jdbc, books, authors);
        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("booksCount", 5L);
        assertThat(health.getDetails()).containsEntry("authorsCount", 3L);
        assertThat(health.getDetails()).containsEntry("h2Version", "2.2.224");
        assertThat(health.getDetails()).containsEntry("h2Mode", "DEFAULT");
    }

    @Test
    void shouldReturnOutOfService_whenEmptyDomainData() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BookRepository books = mock(BookRepository.class);
        AuthorRepository authors = mock(AuthorRepository.class);

        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbc.queryForObject("SELECT H2VERSION()", String.class)).thenReturn("2.2.224");
        when(jdbc.queryForObject("SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME='MODE'", String.class))
                .thenReturn("DEFAULT");
        when(books.count()).thenReturn(0L);
        when(authors.count()).thenReturn(2L);

        var indicator = new CustomHealthIndicator(jdbc, books, authors);
        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("OUT_OF_SERVICE");
        assertThat(health.getDetails()).containsEntry("booksCount", 0L);
    }

    @Test
    void shouldReturnDown_whenDbUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BookRepository books = mock(BookRepository.class);
        AuthorRepository authors = mock(AuthorRepository.class);

        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenThrow(new RuntimeException("DB down"));

        var indicator = new CustomHealthIndicator(jdbc, books, authors);
        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
    }
}
