package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.rest.BookRestController;
import ru.otus.hw.services.BookService;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("REST контроллер для работы с книгами")
@WebMvcTest(BookRestController.class)
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private final AuthorDto author = new AuthorDto(1L, "Author_1");
    private final List<GenreDto> genres = List.of(new GenreDto(1L, "Genre_1"));

    @DisplayName("должен возвращать список всех книг")
    @Test
    void shouldReturnAllBooks() throws Exception {
        List<BookDto> books = List.of(
                new BookDto(1L, "BookTitle_1", author, genres),
                new BookDto(2L, "BookTitle_2", author, genres)
        );
        when(bookService.findAll()).thenReturn(books);

        mockMvc.perform(get("/api/v1/book"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("BookTitle_1"));
    }

    @DisplayName("должен возвращать книгу по id")
    @Test
    void shouldReturnBookById() throws Exception {
        BookDto book = new BookDto(1L, "Test Book", author, genres);
        when(bookService.findById(1L)).thenReturn(book);

        mockMvc.perform(get("/api/v1/book/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @DisplayName("должен создавать книгу с валидными данными")
    @Test
    void shouldCreateBookWithValidData() throws Exception {
        BookCreateDto createDto = new BookCreateDto("Test Book", 1L, List.of(1L));
        BookDto book = new BookDto(1L, "Test Book", author, genres);

        when(bookService.insert(any(BookCreateDto.class))).thenReturn(book);

        mockMvc.perform(post("/api/v1/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @DisplayName("должен обновлять книгу с валидными данными")
    @Test
    void shouldUpdateBookWithValidData() throws Exception {
        BookUpdateDto updateDto = new BookUpdateDto("Updated Book", 1L, List.of(1L));
        BookDto book = new BookDto(1L, "Updated Book", author, genres);

        when(bookService.update(eq(updateDto), eq(1L))).thenReturn(book);

        mockMvc.perform(put("/api/v1/book/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @DisplayName("должен удалять книгу и возвращать статус no content")
    @Test
    void shouldDeleteBookAndReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/book/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteById(1L);
    }
}
