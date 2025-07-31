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
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.mapper.BookMapper;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
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
    private BookMapper bookMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private AuthorMapper authorMapper;

    @Autowired
    private GenreMapper genreMapper;

    @DisplayName("должен возвращать книгу по id")
    @ParameterizedTest(name = "id = {0}")
    @MethodSource("bookIds")
    void shouldReturnCorrectBookById(long id) {
        var entity = bookRepository.findById(id).orElseThrow();
        var expected = bookMapper.toDto(entity);

        var actual = bookService.findById(id);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("должен выбрасывать EntityNotFoundException для несуществующей книги")
    @Test
    void shouldThrowEntityNotFoundExceptionForNonExistingBook() {
        long nonExistingId = 999L;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(nonExistingId));

        assertThat(exception.getMessage())
                .contains("Book not found: " + nonExistingId);
    }

    private static Stream<Long> bookIds() {
        return Stream.of(1L, 2L, 3L);
    }

    @DisplayName("должен возвращать все предзагруженные книги")
    @Test
    void shouldReturnAllPreloadedBooks() {
        List<BookDto> actual = bookService.findAll();
        assertThat(actual)
                .hasSize(3)
                .extracting(
                        BookDto::id,
                        BookDto::title,
                        dto -> dto.author().id(),
                        dto -> dto.genres().size()
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "BookTitle_1", 1L, 3),
                        tuple(2L, "BookTitle_2", 2L, 1),
                        tuple(3L, "BookTitle_3", 3L, 2)
                );
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertNewBook() {
        var author = authorMapper.toDto(authorService.findById(1L));
        var genres = List.of(
                genreMapper.toDto(genreService.findById(1L)),
                genreMapper.toDto(genreService.findById(2L))
        );

        var form = new BookCreateDto("Inserted Book", author.id(),
                genres.stream().map(GenreDto::id).toList());

        BookDto created = bookService.insert(form);

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Inserted Book");
        assertThat(created.author().id()).isEqualTo(1L);
        assertThat(created.genres()).hasSize(2);

        BookDto fetched = bookService.findById(created.id());
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateExistingBook() {
        var author = authorMapper.toDto(authorService.findById(1L));
        var genres = List.of(
                genreMapper.toDto(genreService.findById(1L)),
                genreMapper.toDto(genreService.findById(3L))
        );

        var form = new BookUpdateDto("Updated Title", author.id(),
                genres.stream().map(GenreDto::id).toList());

        BookDto updated = bookService.update(form, 1L);

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(1L);
        assertThat(updated.title()).isEqualTo("Updated Title");
        assertThat(updated.author().id()).isEqualTo(1L);
        assertThat(updated.genres()).hasSize(2);

        BookDto fetched = bookService.findById(1L);
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(updated);
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при обновлении несуществующей книги")
    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistingBook() {
        var author = authorMapper.toDto(authorService.findById(1L));
        var genre = genreMapper.toDto(genreService.findById(1L));

        var form = new BookUpdateDto("Non Existing Book", author.id(), List.of(genre.id()));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.update(form, 999L));

        assertThat(exception.getMessage())
                .contains("Book not found: 999");
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        assertThat(bookService.findById(3L)).isNotNull();

        bookService.deleteById(3L);

        assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(3L));
    }

    @DisplayName("после удаления книги должны каскадно удаляться её комментарии")
    @Test
    void deleteBookShouldCascadeDeleteComments() {
        long bookId = 1L;
        List<CommentDto> commentsBefore = commentService.findAllByBookId(bookId);
        assertThat(commentsBefore).isNotEmpty();

        bookService.deleteById(bookId);

        assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(bookId));

        List<CommentDto> commentsAfter = commentService.findAllByBookId(bookId);
        assertThat(commentsAfter).isEmpty();
    }

    @DisplayName("должен корректно обрабатывать пустой список книг")
    @Test
    void shouldHandleEmptyBookList() {
        bookService.deleteById(1L);
        bookService.deleteById(2L);
        bookService.deleteById(3L);

        List<BookDto> books = bookService.findAll();
        assertThat(books).isEmpty();
    }

    @DisplayName("должен сохранять книгу с минимальными данными")
    @Test
    void shouldInsertBookWithMinimalData() {
        var author = authorMapper.toDto(authorService.findById(1L));
        var genre = genreMapper.toDto(genreService.findById(1L));

        var form = new BookCreateDto("Minimal Book", author.id(), List.of(genre.id()));

        BookDto created = bookService.insert(form);

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Minimal Book");
        assertThat(created.author().id()).isEqualTo(1L);
        assertThat(created.genres()).hasSize(1);
    }

    @DisplayName("должен обновлять только переданные поля книги")
    @Test
    void shouldUpdateOnlyProvidedFields() {
        BookDto original = bookService.findById(1L);

        var form = new BookUpdateDto("Only Title Changed",
                original.author().id(),
                original.genres().stream().map(GenreDto::id).toList());

        BookDto updated = bookService.update(form, 1L);

        assertThat(updated.title()).isEqualTo("Only Title Changed");
        assertThat(updated.author().id()).isEqualTo(original.author().id());
        assertThat(updated.genres()).hasSize(original.genres().size());
    }
}
