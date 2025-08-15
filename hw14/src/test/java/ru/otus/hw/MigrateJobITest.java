package ru.otus.hw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.model.relation.Book;
import ru.otus.hw.model.relation.Comment;
import ru.otus.hw.model.relation.Genre;
import ru.otus.hw.repository.relation.AuthorRepository;
import ru.otus.hw.repository.relation.BookRepository;
import ru.otus.hw.repository.relation.CommentRepository;
import ru.otus.hw.repository.relation.GenreRepository;
import ru.otus.hw.repository.migrate.AuthorMigrateRepository;
import ru.otus.hw.repository.migrate.BookMigrateRepository;
import ru.otus.hw.repository.migrate.CommentMigrateRepository;
import ru.otus.hw.repository.migrate.GenreMigrateRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
class MigrateJobITest {
    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired private AuthorRepository authorRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CommentRepository commentRepository;

    @Autowired private AuthorMigrateRepository authorMigrateRepository;
    @Autowired private GenreMigrateRepository genreMigrateRepository;
    @Autowired private BookMigrateRepository bookMigrateRepository;
    @Autowired private CommentMigrateRepository commentMigrateRepository;

    @BeforeEach
    void clearMeta() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("Полная миграция: корректные данные и соответствия")
    void migrateJob_populatesAllTables_correctly() throws Exception {
        Job job = jobLauncherTestUtils.getJob();
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("migrateJob");

        JobParameters params = new JobParametersBuilder()
                .addLong("run.ts", System.currentTimeMillis())
                .toJobParameters();

        JobExecution exec = jobLauncherTestUtils.launchJob(params);
        assertThat(exec.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        assertThat(authorRepository.count()).isEqualTo(3);
        assertThat(genreRepository.count()).isEqualTo(6);
        assertThat(bookRepository.count()).isEqualTo(3);
        assertThat(commentRepository.count()).isEqualTo(5);

        assertThat(authorMigrateRepository.count()).isEqualTo(3);
        assertThat(genreMigrateRepository.count()).isEqualTo(6);
        assertThat(bookMigrateRepository.count()).isEqualTo(3);
        assertThat(commentMigrateRepository.count()).isEqualTo(5);

        List<Book> books = bookRepository.findAll();
        Map<String, Book> byTitle = books.stream().collect(Collectors.toMap(Book::getTitle, b -> b));

        Book b1 = byTitle.get("BookTitle_1");
        assertThat(b1.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(b1.getGenres().stream().map(Genre::getName).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3");
        assertThat(commentRepository.findAllByBookId(b1.getId()))
                .extracting(Comment::getContent)
                .containsExactlyInAnyOrder("Comment_1_for_BookTitle_1", "Comment_2_for_BookTitle_1");

        Book b2 = byTitle.get("BookTitle_2");
        assertThat(b2.getAuthor().getFullName()).isEqualTo("Author_2");
        assertThat(b2.getGenres().stream().map(Genre::getName).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("Genre_4");
        assertThat(commentRepository.findAllByBookId(b2.getId()))
                .extracting(Comment::getContent)
                .containsExactlyInAnyOrder("Comment_1_for_BookTitle_2", "Comment_2_for_BookTitle_2");

        Book b3 = byTitle.get("BookTitle_3");
        assertThat(b3.getAuthor().getFullName()).isEqualTo("Author_3");
        assertThat(b3.getGenres().stream().map(Genre::getName).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("Genre_5", "Genre_6");
        assertThat(commentRepository.findAllByBookId(b3.getId()))
                .extracting(Comment::getContent)
                .containsExactlyInAnyOrder("Comment_1_for_BookTitle_3");
    }

    @Test
    @DisplayName("Повторный запуск не даёт дублей")
    void migrateJob_isIdempotent_onSecondRun() throws Exception {
        JobParameters p1 = new JobParametersBuilder()
                .addLong("run.ts", System.currentTimeMillis())
                .toJobParameters();
        JobExecution e1 = jobLauncherTestUtils.launchJob(p1);
        assertThat(e1.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        long a1 = authorRepository.count();
        long g1 = genreRepository.count();
        long b1 = bookRepository.count();
        long c1 = commentRepository.count();

        long am1 = authorMigrateRepository.count();
        long gm1 = genreMigrateRepository.count();
        long bm1 = bookMigrateRepository.count();
        long cm1 = commentMigrateRepository.count();

        JobParameters p2 = new JobParametersBuilder()
                .addLong("run.ts", System.currentTimeMillis() + 1)
                .toJobParameters();
        JobExecution e2 = jobLauncherTestUtils.launchJob(p2);
        assertThat(e2.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        assertThat(authorRepository.count()).isEqualTo(a1);
        assertThat(genreRepository.count()).isEqualTo(g1);
        assertThat(bookRepository.count()).isEqualTo(b1);
        assertThat(commentRepository.count()).isEqualTo(c1);

        assertThat(authorMigrateRepository.count()).isEqualTo(am1);
        assertThat(genreMigrateRepository.count()).isEqualTo(gm1);
        assertThat(bookMigrateRepository.count()).isEqualTo(bm1);
        assertThat(commentMigrateRepository.count()).isEqualTo(cm1);
    }
}
