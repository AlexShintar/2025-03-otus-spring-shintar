package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.rest.AuthorRestController;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("REST контроллер для работы с авторами")
@WebMvcTest(AuthorRestController.class)
class AuthorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @DisplayName("должен возвращать список всех авторов")
    @Test
    void shouldReturnAllAuthors() throws Exception {
        List<AuthorDto> authors = List.of(
                new AuthorDto(1L, "Author_1"),
                new AuthorDto(2L, "Author_2"),
                new AuthorDto(3L, "Author_3")
        );
        when(authorService.findAll()).thenReturn(authors);

        mockMvc.perform(get("/api/v1/author"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fullName").value("Author_1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].fullName").value("Author_2"));

        verify(authorService, times(1)).findAll();
    }
}
