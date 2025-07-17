package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.otus.hw.controllers.BookController;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.converters.BookConverter;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(controllers = BookController.class)
class BookControllerSecurityTest {

    private static final String LOGIN_REDIRECT_URL = "http://localhost/login";
    private static final String SUCCESS_REDIRECT_URL = "/";
    private static final String TEST_USER = "testuser";

    private static final Long TEST_BOOK_ID = 1L;
    private static final Long TEST_AUTHOR_ID = 1L;
    private static final Long TEST_GENRE_ID = 1L;
    private static final String TEST_BOOK_TITLE = "BookTitle_1";
    private static final String TEST_AUTHOR_NAME = "Author_1";
    private static final String TEST_GENRE_NAME = "Genre_1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookConverter bookConverter;

    private static BookDto createTestBook() {
        return new BookDto(
                TEST_BOOK_ID,
                TEST_BOOK_TITLE,
                new AuthorDto(TEST_AUTHOR_ID, TEST_AUTHOR_NAME),
                List.of(new GenreDto(TEST_GENRE_ID, TEST_GENRE_NAME))
        );
    }

    private static BookUpdateDto createTestBookUpdateDto() {
        return new BookUpdateDto(
                TEST_BOOK_ID,
                TEST_BOOK_TITLE,
                TEST_AUTHOR_ID,
                List.of(TEST_GENRE_ID)
        );
    }

    private static List<AuthorDto> createTestAuthors() {
        return List.of(new AuthorDto(TEST_AUTHOR_ID, TEST_AUTHOR_NAME));
    }

    private static List<GenreDto> createTestGenres() {
        return List.of(new GenreDto(TEST_GENRE_ID, TEST_GENRE_NAME));
    }

    private void setupBasicMocks() {
        when(bookService.findById(anyLong())).thenReturn(createTestBook());
        when(authorService.findAll()).thenReturn(createTestAuthors());
        when(genreService.findAll()).thenReturn(createTestGenres());
    }

    private void setupEditMocks() {
        setupBasicMocks();
        when(bookConverter.toFormDto(any())).thenReturn(createTestBookUpdateDto());
    }

    private void executeRequestAndVerify(MockHttpServletRequestBuilder requestBuilder,
                                         @Nullable String userName,
                                         int expectedStatus,
                                         @Nullable String expectedRedirectUrl) throws Exception {
        if (userName != null) {
            requestBuilder.with(user(userName));
        }

        ResultActions resultActions = mockMvc.perform(requestBuilder)
                .andExpect(status().is(expectedStatus));

        if (expectedRedirectUrl != null) {
            resultActions.andExpect(redirectedUrl(expectedRedirectUrl));
        }
    }

    @DisplayName("Тест доступа к списку книг")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("getRequestsTestData")
    void testBookListAccess(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupBasicMocks();
        executeRequestAndVerify(MockMvcRequestBuilders.get("/"), userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест доступа к детальной информации о книге")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("getRequestsTestData")
    void testBookDetailAccess(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupBasicMocks();
        executeRequestAndVerify(MockMvcRequestBuilders.get("/book/{id}", TEST_BOOK_ID), userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест доступа к форме создания книги")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("getRequestsTestData")
    void testBookCreationFormAccess(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupBasicMocks();
        executeRequestAndVerify(MockMvcRequestBuilders.get("/book/new"), userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест доступа к форме редактирования книги")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("getRequestsTestData")
    void testBookEditFormAccess(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupEditMocks();
        executeRequestAndVerify(MockMvcRequestBuilders.get("/book/{id}/edit", TEST_BOOK_ID), userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест безопасности при создании книги")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("modifyingRequestsTestData")
    void testBookCreationSecurity(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupBasicMocks();
        var requestBuilder = MockMvcRequestBuilders.post("/book")
                .param("title", "New Book Title")
                .param("authorId", String.valueOf(TEST_AUTHOR_ID))
                .param("genreIds", String.valueOf(TEST_GENRE_ID))
                .with(csrf());
        executeRequestAndVerify(requestBuilder, userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест безопасности при обновлении книги")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("modifyingRequestsTestData")
    void testBookUpdateSecurity(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupBasicMocks();
        var requestBuilder = MockMvcRequestBuilders.put("/book/{id}", TEST_BOOK_ID)
                .param("title", "Updated Book Title")
                .param("authorId", String.valueOf(TEST_AUTHOR_ID))
                .param("genreIds", String.valueOf(TEST_GENRE_ID))
                .with(csrf());
        executeRequestAndVerify(requestBuilder, userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест безопасности при удалении книги")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("modifyingRequestsTestData")
    void testBookDeletionSecurity(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        setupBasicMocks();
        var requestBuilder = MockMvcRequestBuilders.delete("/book/{id}", TEST_BOOK_ID)
                .with(csrf());
        executeRequestAndVerify(requestBuilder, userName, expectedStatus, expectedRedirectUrl);
    }

    private static Stream<Arguments> getRequestsTestData() {
        return Stream.of(
                Arguments.of(TEST_USER, 200, null),
                Arguments.of(null, 302, LOGIN_REDIRECT_URL)
        );
    }

    private static Stream<Arguments> modifyingRequestsTestData() {
        return Stream.of(
                Arguments.of(TEST_USER, 302, SUCCESS_REDIRECT_URL),
                Arguments.of(null, 302, LOGIN_REDIRECT_URL)
        );
    }
}
