package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Сервис для работы с книгами")
@Transactional(propagation = Propagation.NEVER)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookConverter bookConverter;

    @Autowired
    private CommentService commentService;

    @DisplayName("должен возвращать книгу по id")
    @ParameterizedTest(name = "id = {0}")
    @MethodSource("bookIds")
    void shouldReturnCorrectBookById(long id) {
        Book entity = bookRepository.findById(id).orElseThrow();
        BookDto expected = bookConverter.toDto(entity);

        Optional<BookDto> actualOpt = bookService.findById(id);
        assertThat(actualOpt).isPresent();
        assertThat(actualOpt.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private static Stream<Long> bookIds() {
        return Stream.of(1L, 2L, 3L);
    }

    @DisplayName("должен возвращать все предзагруженные книги")
    @Test
    void shouldReturnAllPreloadedBooks() {
        List<BookDto> expected = bookRepository.findAll().stream()
                .map(bookConverter::toDto)
                .toList();

        List<BookDto> actual = bookService.findAll();
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertNewBook() {
        BookDto created = bookService.insert("Inserted Book", 1L, Set.of(1L, 2L));
        BookDto fetched = bookService.findById(created.getId()).orElseThrow();
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateExistingBook() {
        BookDto updated = bookService.update(1L, "Updated Title", 1L, Set.of(1L, 3L));

        Book entity = bookRepository.findById(1L).orElseThrow();
        BookDto expected = bookConverter.toDto(entity);

        assertThat(updated)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        assertThat(bookService.findById(3L)).isPresent();
        bookService.deleteById(3L);
        assertThat(bookService.findById(3L)).isEmpty();
    }

    @DisplayName("после удаления книги должны каскадно удалять её комментарии")
    @Test
    void deleteBookShouldCascadeDeleteComments() {
        long bookId = 1L;
        List<CommentDto> commentsBefore = commentService.findAllByBookId(bookId);
        assertThat(commentsBefore).isNotEmpty();

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
        List<CommentDto> commentsAfter = commentService.findAllByBookId(bookId);
        assertThat(commentsAfter).isEmpty();
    }

    @DisplayName("insert с несуществующим автором должен бросать EntityNotFoundException")
    @Test
    void insertInvalidAuthorThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> bookService.insert("X", 999L, Set.of(1L)));
    }

    @DisplayName("insert с пустым списком жанров должен бросать IllegalArgumentException")
    @Test
    void insertEmptyGenresThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> bookService.insert("X", 1L, Set.of()));
    }

    @DisplayName("update для несуществующей книги должен бросать EntityNotFoundException")
    @Test
    void updateNonExistingThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> bookService.update(999L, "X", 1L, Set.of(1L)));
    }
}
