package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser; // <-- Не забудьте импортировать
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
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
    private BookConverter bookConverter;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private AuthorConverter authorConverter;

    @Autowired
    private GenreConverter genreConverter;

    // Этот тест не требует авторизации, так как findById публичный
    @DisplayName("должен возвращать книгу по id")
    @ParameterizedTest(name = "id = {0}")
    @MethodSource("bookIds")
    void shouldReturnCorrectBookById(long id) {
        var entity = bookRepository.findById(id).orElseThrow();
        var expected = bookConverter.toDto(entity);

        var actual = bookService.findById(id);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    // Этот тест тоже не требует авторизации
    @DisplayName("должен выбрасывать EntityNotFoundException для несуществующей книги")
    @Test
    void shouldThrowEntityNotFoundExceptionForNonExistingBook() {
        long nonExistingId = 999L;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(nonExistingId));

        assertThat(exception.getMessage())
                .contains("Book with id " + nonExistingId + " not found");
    }

    private static Stream<Long> bookIds() {
        return Stream.of(1L, 2L, 3L);
    }

    // Этот тест тоже не требует авторизации
    @DisplayName("должен возвращать все предзагруженные книги")
    @Test
    void shouldReturnAllPreloadedBooks() {
        List<BookDto> actual = bookService.findAll();
        assertThat(actual)
                .hasSize(3)
                .extracting(
                        BookDto::getId,
                        BookDto::getTitle,
                        dto -> dto.getAuthor().getId(),
                        dto -> dto.getGenres().size()
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "BookTitle_1", 1L, 3),
                        tuple(2L, "BookTitle_2", 2L, 1),
                        tuple(3L, "BookTitle_3", 3L, 2)
                );
    }

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertNewBook() {
        Author authorEntity = authorService.findById(1L);
        AuthorDto author = authorConverter.toDto(authorEntity);

        List<Genre> genreEntities = List.of(
                genreService.findById(1L),
                genreService.findById(2L)
        );
        List<GenreDto> genres = genreEntities.stream()
                .map(genreConverter::toDto)
                .toList();

        BookDto newBook = new BookDto();
        newBook.setTitle("Inserted Book");
        newBook.setAuthor(author);
        newBook.setGenres(genres);

        BookDto created = bookService.insert(newBook);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Inserted Book");
        assertThat(created.getAuthor().getId()).isEqualTo(1L);
        assertThat(created.getGenres()).hasSize(2);

        BookDto fetched = bookService.findById(created.getId());
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateExistingBook() {
        Author authorEntity = authorService.findById(1L);
        AuthorDto author = authorConverter.toDto(authorEntity);

        List<Genre> genreEntities = List.of(
                genreService.findById(1L),
                genreService.findById(3L)
        );
        List<GenreDto> genres = genreEntities.stream()
                .map(genreConverter::toDto)
                .toList();

        BookDto updateDto = new BookDto();
        updateDto.setId(1L);
        updateDto.setTitle("Updated Title");
        updateDto.setAuthor(author);
        updateDto.setGenres(genres);

        BookDto updated = bookService.update(updateDto);

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getAuthor().getId()).isEqualTo(1L);
        assertThat(updated.getGenres()).hasSize(2);


        BookDto fetched = bookService.findById(1L);
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(updated);
    }

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен выбрасывать EntityNotFoundException при обновлении несуществующей книги")
    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistingBook() {
        Author authorEntity = authorService.findById(1L);
        AuthorDto author = authorConverter.toDto(authorEntity);

        Genre genreEntity = genreService.findById(1L);
        GenreDto genre = genreConverter.toDto(genreEntity);

        BookDto nonExisting = new BookDto();
        nonExisting.setId(999L);
        nonExisting.setTitle("Non Existing Book");
        nonExisting.setAuthor(author);
        nonExisting.setGenres(List.of(genre));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.update(nonExisting));

        assertThat(exception.getMessage())
                .contains("Book with id " + nonExisting.getId() + " not found");
    }

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        assertThat(bookService.findById(3L)).isNotNull();

        bookService.deleteById(3L);

        assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(3L));
    }


    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
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

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен корректно обрабатывать пустой список книг")
    @Test
    void shouldHandleEmptyBookList() {
        bookService.deleteById(1L);
        bookService.deleteById(2L);
        bookService.deleteById(3L);

        List<BookDto> books = bookService.findAll();
        assertThat(books).isEmpty();
    }

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен сохранять книгу с минимальными данными")
    @Test
    void shouldInsertBookWithMinimalData() {
        Author authorEntity = authorService.findById(1L);
        AuthorDto author = authorConverter.toDto(authorEntity);

        Genre genreEntity = genreService.findById(1L);
        GenreDto genre = genreConverter.toDto(genreEntity);

        BookDto minimalBook = new BookDto();
        minimalBook.setTitle("Minimal Book");
        minimalBook.setAuthor(author);
        minimalBook.setGenres(List.of(genre));

        BookDto created = bookService.insert(minimalBook);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Minimal Book");
        assertThat(created.getAuthor().getId()).isEqualTo(1L);
        assertThat(created.getGenres()).hasSize(1);
    }

    @WithMockUser(roles = "ADMIN") // <-- Добавлена аннотация
    @DisplayName("должен обновлять только переданные поля книги")
    @Test
    void shouldUpdateOnlyProvidedFields() {

        BookDto original = bookService.findById(1L);

        BookDto updateDto = new BookDto();
        updateDto.setId(1L);
        updateDto.setTitle("Only Title Changed");
        updateDto.setAuthor(original.getAuthor());
        updateDto.setGenres(original.getGenres());

        BookDto updated = bookService.update(updateDto);

        assertThat(updated.getTitle()).isEqualTo("Only Title Changed");
        assertThat(updated.getAuthor().getId()).isEqualTo(original.getAuthor().getId());
        assertThat(updated.getGenres()).hasSize(original.getGenres().size());
    }
}
