package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Тестирование правил доступа для сервиса книг")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookServiceAclTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    private static final long EXISTING_BOOK_ID = 1L;

    @Test
    @DisplayName("Администратор может создать книгу")
    @WithMockUser(username = "vetinari", roles = "ADMIN")
    void adminCanCreateBook() {
        BookDto newBookDto = getNewBookDto();
        assertDoesNotThrow(() -> {
            bookService.insert(newBookDto);
        });
    }

    @Test
    @DisplayName("Обычный пользователь НЕ может создать книгу")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCannotCreateBook() {
        BookDto newBookDto = getNewBookDto();
        assertThrows(AccessDeniedException.class, () -> bookService.insert(newBookDto));
    }

    @Test
    @DisplayName("Обычный пользователь НЕ может удалить книгу")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCannotDeleteBook() {
        assertThrows(AccessDeniedException.class, () -> bookService.deleteById(EXISTING_BOOK_ID));
    }

    @Test
    @DisplayName("Обычный пользователь может читать список всех книг")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCanFindAllBooks() {
        List<BookDto> books = assertDoesNotThrow(() -> bookService.findAll());
        assertThat(books).hasSize(3);
    }

    @Test
    @DisplayName("Обычный пользователь может читать детали одной книги")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCanFindBookById() {
        BookDto book = assertDoesNotThrow(() -> bookService.findById(EXISTING_BOOK_ID));
        assertThat(book.getId()).isEqualTo(EXISTING_BOOK_ID);
    }

    @Test
    @DisplayName("Анонимный пользователь НЕ может создать книгу")
    @WithAnonymousUser
    void anonymousCannotCreateBook() {
        BookDto newBookDto = getNewBookDto();
        assertThrows(AuthorizationDeniedException.class, () -> bookService.insert(newBookDto));
    }

    private BookDto getNewBookDto() {
        Author author = authorService.findById(1L);
        Genre genre = genreService.findById(1L);
        BookDto bookDto = new BookDto();
        bookDto.setTitle("Test Book");
        bookDto.setAuthor(new AuthorDto(author.getId(), author.getFullName()));
        bookDto.setGenres(List.of(new GenreDto(genre.getId(), genre.getName())));
        return bookDto;
    }
}
