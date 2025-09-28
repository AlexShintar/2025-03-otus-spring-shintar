package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.otus.hw.controllers.BookController;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(controllers = BookController.class)
class BookControllerSecurityTest {

    private static final String LOGIN_REDIRECT_URL = "http://localhost/login";
    private static final String SUCCESS_REDIRECT_URL = "/";

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
        when(bookService.findAll()).thenReturn(List.of(createTestBook()));
        when(bookService.findById(anyLong())).thenReturn(createTestBook());
        when(authorService.findAll()).thenReturn(createTestAuthors());
        when(genreService.findAll()).thenReturn(createTestGenres());
        when(commentService.findAllByBookId(anyLong())).thenReturn(List.of());
    }

    private void setupEditMocks() {
        setupBasicMocks();
        when(bookConverter.toFormDto(any())).thenReturn(createTestBookUpdateDto());
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asUser() {
        return user("user");
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asAdmin() {
        return user("admin").roles("ADMIN");
    }

    private void performAndAssert(MockHttpServletRequestBuilder request,
                                  @Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                                  int expectedStatus,
                                  @Nullable String expectedRedirectUrl) throws Exception {
        if (principal != null) {
            request.with(principal);
        }
        ResultActions ra = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus));
        if (expectedRedirectUrl != null) {
            ra.andExpect(redirectedUrl(expectedRedirectUrl));
        }
    }

    @DisplayName("GET / — список книг: аноним -> 302 login, user/admin -> 200")
    @ParameterizedTest
    @MethodSource("publicGetData")
    void bookListAccess(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                        int expectedStatus,
                        @Nullable String redirect) throws Exception {
        setupBasicMocks();
        performAndAssert(MockMvcRequestBuilders.get("/"), principal, expectedStatus, redirect);
    }

    @DisplayName("GET /book/{id} — детали книги: аноним -> 302 login, user/admin -> 200")
    @ParameterizedTest
    @MethodSource("publicGetData")
    void bookDetailAccess(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                          int expectedStatus,
                          @Nullable String redirect) throws Exception {
        setupBasicMocks();
        performAndAssert(MockMvcRequestBuilders.get("/book/{id}", TEST_BOOK_ID), principal, expectedStatus, redirect);
    }

    @DisplayName("GET /book/new — только ADMIN: аноним -> 302 login, user -> 302 /access-denied, admin -> 200")
    @ParameterizedTest
    @MethodSource("adminGetData")
    void bookCreationFormAccess(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                                int expectedStatus,
                                @Nullable String redirect) throws Exception {
        setupBasicMocks();
        performAndAssert(MockMvcRequestBuilders.get("/book/new"), principal, expectedStatus, redirect);
    }

    @DisplayName("GET /book/{id}/edit — только ADMIN: аноним -> 302 login, user -> 302 /access-denied, admin -> 200")
    @ParameterizedTest
    @MethodSource("adminGetData")
    void bookEditFormAccess(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                            int expectedStatus,
                            @Nullable String redirect) throws Exception {
        setupEditMocks();
        performAndAssert(MockMvcRequestBuilders.get("/book/{id}/edit", TEST_BOOK_ID), principal, expectedStatus, redirect);
    }

    @DisplayName("POST /book — только ADMIN: аноним -> 302 login, user -> 302 /access-denied, admin -> 302 /")
    @ParameterizedTest
    @MethodSource("adminWriteData")
    void bookCreationSecurity(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                              int expectedStatus,
                              @Nullable String redirect) throws Exception {
        setupBasicMocks();
        var req = MockMvcRequestBuilders.post("/book")
                .param("title", "New Book Title")
                .param("authorId", String.valueOf(TEST_AUTHOR_ID))
                .param("genreIds", String.valueOf(TEST_GENRE_ID))
                .with(csrf());
        performAndAssert(req, principal, expectedStatus, redirect);
    }

    @DisplayName("PUT /book/{id} — только ADMIN: аноним -> 302 login, user -> 302 /access-denied, admin -> 302 /")
    @ParameterizedTest
    @MethodSource("adminWriteData")
    void bookUpdateSecurity(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                            int expectedStatus,
                            @Nullable String redirect) throws Exception {
        setupBasicMocks();
        var req = MockMvcRequestBuilders.put("/book/{id}", TEST_BOOK_ID)
                .param("title", "Updated Book Title")
                .param("authorId", String.valueOf(TEST_AUTHOR_ID))
                .param("genreIds", String.valueOf(TEST_GENRE_ID))
                .with(csrf());
        performAndAssert(req, principal, expectedStatus, redirect);
    }

    @DisplayName("DELETE /book/{id} — только ADMIN: аноним -> 302 login, user -> 302 /access-denied, admin -> 302 /")
    @ParameterizedTest
    @MethodSource("adminWriteData")
    void bookDeletionSecurity(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                              int expectedStatus,
                              @Nullable String redirect) throws Exception {
        setupBasicMocks();
        var req = MockMvcRequestBuilders.delete("/book/{id}", TEST_BOOK_ID)
                .with(csrf());
        performAndAssert(req, principal, expectedStatus, redirect);
    }

    private static Stream<Arguments> publicGetData() {
        return Stream.of(
                Arguments.of(null, 302, LOGIN_REDIRECT_URL),
                Arguments.of(asUser(), 200, null),
                Arguments.of(asAdmin(), 200, null)
        );
    }

    private static Stream<Arguments> adminGetData() {
        return Stream.of(
                Arguments.of(null, 302, LOGIN_REDIRECT_URL),
                Arguments.of(asUser(), 302, "/access-denied"),
                Arguments.of(asAdmin(), 200, null)
        );
    }

    private static Stream<Arguments> adminWriteData() {
        return Stream.of(
                Arguments.of(null, 302, LOGIN_REDIRECT_URL),
                Arguments.of(asUser(), 302, "/access-denied"),
                Arguments.of(asAdmin(), 302, SUCCESS_REDIRECT_URL)
        );
    }
}
