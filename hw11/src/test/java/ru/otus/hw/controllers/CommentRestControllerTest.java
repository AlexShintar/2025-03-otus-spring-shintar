package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.rest.CommentRestController;
import ru.otus.hw.services.CommentService;

import java.util.List;

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

@DisplayName("REST контроллер для работы с комментариями")
@WebMvcTest(CommentRestController.class)
class CommentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("должен возвращать список всех комментариев для книги")
    @Test
    void shouldReturnAllCommentsForBook() throws Exception {
        List<CommentDto> comments = List.of(
                new CommentDto(1L, "Comment_1"),
                new CommentDto(2L, "Comment_2")
        );
        when(commentService.findAllByBookId(1L)).thenReturn(comments);

        mockMvc.perform(get("/api/v1/book/{bookId}/comment", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("Comment_1"));

        verify(commentService, times(1)).findAllByBookId(1L);
    }

    @DisplayName("должен создавать комментарий с валидными данными")
    @Test
    void shouldCreateCommentWithValidData() throws Exception {
        CommentDto commentDto = new CommentDto(null, "Test comment");
        CommentDto created = new CommentDto(1L, "Test comment");

        when(commentService.insert(eq("Test comment"), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/api/v1/book/{bookId}/comment", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/book/1/comment/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test comment"));

        verify(commentService, times(1)).insert("Test comment", 1L);
    }

    @DisplayName("должен обновлять комментарий с валидными данными")
    @Test
    void shouldUpdateCommentWithValidData() throws Exception {
        CommentDto commentDto = new CommentDto(null, "Updated comment");
        CommentDto updated = new CommentDto(1L, "Updated comment");

        when(commentService.update(eq(1L), eq(1L), eq("Updated comment"))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/book/{bookId}/comment/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Updated comment"));

        verify(commentService, times(1)).update(1L, 1L, "Updated comment");
    }

    @DisplayName("должен удалять комментарий и возвращать статус no content")
    @Test
    void shouldDeleteCommentAndReturnNoContent() throws Exception {
        doNothing().when(commentService).deleteById(1L, 1L);

        mockMvc.perform(delete("/api/v1/book/{bookId}/comment/{commentId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteById(1L, 1L);
    }
}
