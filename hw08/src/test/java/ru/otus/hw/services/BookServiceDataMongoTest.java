package ru.otus.hw.services;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.testchangelog.TestDatabaseChangelog;
import ru.otus.hw.testutil.TestDataFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Сервис для работы с книгами")
@DataMongoTest
@EnableMongock
@Import({TestDatabaseChangelog.class,
        BookServiceImpl.class,
        CommentServiceImpl.class,
        BookConverter.class,
        AuthorConverter.class,
        GenreConverter.class,
        CommentConverter.class})
class BookServiceDataMongoTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookConverter bookConverter;

    private static final List<Book> initialBooks = TestDataFactory.books();
    private static final List<String> allGenreIds = TestDataFactory.genres()
            .stream()
            .map(Genre::getId)
            .collect(Collectors.toList());

    @DisplayName("должен возвращать книгу по id для всех предзагруженных")
    @ParameterizedTest(name = "id = {0}")
    @MethodSource("bookIds")
    void shouldReturnCorrectBookById(String bookId) {
        Book expectedEntity = initialBooks.stream()
                .filter(b -> b.getId().equals(bookId))
                .findFirst()
                .orElseThrow();
        BookDto expected = bookConverter.toDto(expectedEntity);

        Optional<BookDto> actualOpt = bookService.findById(bookId);
        assertThat(actualOpt)
                .as("Book %s should be found", bookId)
                .isPresent();
        assertThat(actualOpt.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private static Stream<String> bookIds() {
        return initialBooks.stream().map(Book::getId);
    }

    @DisplayName("должен возвращать все предзагруженные книги")
    @Test
    void shouldReturnAllPreloadedBooks() {
        List<BookDto> actual = bookService.findAll();
        assertThat(actual)
                .hasSize(initialBooks.size())
                .extracting(
                        BookDto::title,
                        dto -> dto.author().fullName(),
                        dto -> dto.genres().size()
                )
                .containsExactlyInAnyOrder(
                        tuple("BookTitle_1", "Author_1", 3),
                        tuple("BookTitle_2", "Author_2", 1),
                        tuple("BookTitle_3", "Author_3", 2)
                );
    }

    @DirtiesContext
    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertNewBook() {
        String authorId = TestDataFactory.authors().get(0).getId();
        Set<String> genreIds = new HashSet<>(allGenreIds.subList(0, 2));

        BookDto created = bookService.insert("Inserted Book", authorId, genreIds);
        BookDto fetched = bookService.findById(created.id()).orElseThrow();
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @DirtiesContext
    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateExistingBook() {
        Book original = initialBooks.get(0);
        String bookId = original.getId();
        String newAuthorId = TestDataFactory.authors().get(1).getId();
        Set<String> newGenreIds = new HashSet<>(allGenreIds.subList(2, 4));

        // Выполняем обновление и сразу получаем из сервиса результат актуального объекта из БД
        BookDto updated = bookService.update(bookId, "Updated Title", newAuthorId, newGenreIds);
        // Убеждаемся, что возвращённый DTO соответствует записям в БД
        BookDto fetched = bookService.findById(bookId).orElseThrow();

        assertThat(updated.id()).isEqualTo(bookId);
        assertThat(updated)
                .usingRecursiveComparison()
                .isEqualTo(fetched);
    }

    @DirtiesContext
    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        String bookId = initialBooks.get(2).getId();
        assertThat(bookService.findById(bookId)).isPresent();
        bookService.deleteById(bookId);
        assertThat(bookService.findById(bookId)).isEmpty();
    }

    @DirtiesContext
    @DisplayName("после удаления книги должны каскадно удалять её комментарии")
    @Test
    void deleteBookShouldCascadeDeleteComments() {
        String bookId = initialBooks.get(0).getId();
        List<CommentDto> before = commentService.findAllByBookId(bookId);
        assertThat(before).isNotEmpty();

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
        List<CommentDto> after = commentService.findAllByBookId(bookId);
        assertThat(after).isEmpty();
    }

    @DisplayName("insert с несуществующим автором должен бросать EntityNotFoundException")
    @Test
    void insertInvalidAuthorThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> bookService.insert(
                        "X", "invalid-id",
                        Set.of(allGenreIds.get(0))
                )
        );
    }

    @DisplayName("insert с пустым списком жанров должен бросать IllegalArgumentException")
    @Test
    void insertEmptyGenresThrows() {
        String authorId = TestDataFactory.authors().get(0).getId();
        assertThrows(IllegalArgumentException.class,
                () -> bookService.insert("X", authorId, Set.of())
        );
    }

    @DisplayName("update для несуществующей книги должен бросать EntityNotFoundException")
    @Test
    void updateNonExistingThrows() {
        String authorId = TestDataFactory.authors().get(0).getId();
        assertThrows(EntityNotFoundException.class,
                () -> bookService.update(
                        "invalid-id", "X", authorId,
                        Set.of(allGenreIds.get(0))
                )
        );
    }
}
