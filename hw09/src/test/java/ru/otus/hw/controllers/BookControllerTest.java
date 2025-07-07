package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер для работы с книгами")
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookConverter bookConverter;

    private List<AuthorDto> authors;
    private List<GenreDto> genres;
    private List<BookDto> books;
    private BookDto book;
    private BookFormDto bookForm;
    private List<CommentDto> comments;

    @BeforeEach
    void setUp() {
        authors = List.of(
                new AuthorDto(1L, "Author_1"),
                new AuthorDto(2L, "Author_2")
        );

        genres = List.of(
                new GenreDto(1L, "Genre_1"),
                new GenreDto(2L, "Genre_2")
        );

        books = List.of(
                new BookDto(1L, "BookTitle_1", authors.get(0), genres),
                new BookDto(2L, "BookTitle_2", authors.get(1), genres)
        );

        book = new BookDto(1L, "Test Book", authors.get(0), genres);

        bookForm = new BookFormDto();
        bookForm.setId(1L);
        bookForm.setTitle("Test Book");
        bookForm.setAuthorId(1L);
        bookForm.setGenreIds(List.of(1L, 2L));

        comments = List.of(
                new CommentDto(1L, "Comment_1"),
                new CommentDto(2L, "Comment_2")
        );
    }

    @DisplayName("должен возвращать представление списка книг")
    @Test
    void shouldReturnListView() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookService.findAll()).thenReturn(books);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres))
                .andExpect(model().attribute("bookList", books));

        verify(bookService, times(1)).findAll();
    }

    @DisplayName("должен возвращать представление детальной информации о книге")
    @Test
    void shouldReturnDetailView() throws Exception {
        long bookId = 1L;
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookService.findById(bookId)).thenReturn(book);
        when(commentService.findAllByBookId(bookId)).thenReturn(comments);

        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("comments", comments));

        verify(bookService, times(1)).findById(bookId);
        verify(commentService, times(1)).findAllByBookId(bookId);
    }

    @DisplayName("должен возвращать форму создания новой книги")
    @Test
    void shouldReturnNewBookForm() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        mockMvc.perform(get("/book/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres))
                .andExpect(model().attributeExists("book"));
    }

    @DisplayName("должен возвращать форму редактирования книги")
    @Test
    void shouldReturnEditBookForm() throws Exception {
        long bookId = 1L;
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookService.findById(bookId)).thenReturn(book);
        when(bookConverter.toFormDto(book)).thenReturn(bookForm);

        mockMvc.perform(get("/book/{id}/edit", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres))
                .andExpect(model().attribute("book", bookForm));

        verify(bookService, times(1)).findById(bookId);
        verify(bookConverter, times(1)).toFormDto(book);
    }

    @DisplayName("должен создавать новую книгу с валидными данными и перенаправлять на главную")
    @Test
    void shouldCreateBookWithValidDataAndRedirectToRoot() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookConverter.fromFormDto(any(BookFormDto.class), eq(null))).thenReturn(book);
        when(bookService.insert(any(BookDto.class))).thenReturn(book);

        mockMvc.perform(post("/book")
                        .param("title", "Test Book")
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService, times(1)).insert(any(BookDto.class));
    }

    @DisplayName("должен возвращать форму с ошибками при создании книги с невалидными данными")
    @Test
    void shouldReturnFormWithErrorsWhenCreatingBookWithInvalidData() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        mockMvc.perform(post("/book")
                        .param("title", "")  // Пустое название - валидационная ошибка
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().hasErrors());

        verify(bookService, times(0)).insert(any(BookDto.class));
    }

    @DisplayName("должен обновлять книгу с валидными данными и перенаправлять на главную")
    @Test
    void shouldUpdateBookWithValidDataAndRedirectToRoot() throws Exception {
        long bookId = 1L;
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookConverter.fromFormDto(any(BookFormDto.class), eq(bookId))).thenReturn(book);
        when(bookService.update(any(BookDto.class))).thenReturn(book);

        mockMvc.perform(put("/book/{id}", bookId)
                        .param("title", "Updated Book")
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService, times(1)).update(any(BookDto.class));
    }

    @DisplayName("должен возвращать форму с ошибками при обновлении книги с невалидными данными")
    @Test
    void shouldReturnFormWithErrorsWhenUpdatingBookWithInvalidData() throws Exception {
        long bookId = 1L;
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        mockMvc.perform(put("/book/{id}", bookId)
                        .param("title", "")  // Пустое название - валидационная ошибка
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().hasErrors());

        verify(bookService, times(0)).update(any(BookDto.class));
    }

    @DisplayName("должен удалять книгу и перенаправлять на главную")
    @Test
    void shouldDeleteBookAndRedirectToRoot() throws Exception {
        long bookId = 1L;
        doNothing().when(bookService).deleteById(bookId);

        mockMvc.perform(delete("/book/{id}", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService, times(1)).deleteById(bookId);
    }

    @DisplayName("должен возвращать форму с ошибками при создании книги со слишком коротким названием")
    @Test
    void shouldReturnFormWithErrorsWhenCreatingBookWithTitleTooShort() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        mockMvc.perform(post("/book")
                        .param("title", "A")
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().hasErrors());

        verify(bookService, times(0)).insert(any(BookDto.class));
    }

    @DisplayName("должен удалять несуществующую книгу без ошибок и перенаправлять на главную")
    @Test
    void shouldDeleteNonExistentBookWithoutErrorsAndRedirectToRoot() throws Exception {
        long bookId = 999L;
        doNothing().when(bookService).deleteById(bookId);

        mockMvc.perform(delete("/book/{id}", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService, times(1)).deleteById(bookId);
    }

    @DisplayName("должен заполнять модель списком авторов")
    @Test
    void shouldPopulateModelWithAuthors() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookService.findAll()).thenReturn(books);

        mockMvc.perform(get("/"))
                .andExpect(model().attribute("authors", authors));

        verify(authorService, times(1)).findAll();
    }

    @DisplayName("должен заполнять модель списком жанров")
    @Test
    void shouldPopulateModelWithGenres() throws Exception {
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);
        when(bookService.findAll()).thenReturn(books);

        mockMvc.perform(get("/"))
                .andExpect(model().attribute("genres", genres));

        verify(genreService, times(1)).findAll();
    }
}
