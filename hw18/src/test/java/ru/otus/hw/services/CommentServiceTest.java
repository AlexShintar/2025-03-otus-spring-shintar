package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.mapper.CommentMapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Сервис для работы с комментариями")
@Transactional(propagation = Propagation.NEVER)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @MockitoBean
    private ExternalBookRecommendationService externalBookRecommendationService;

    @DisplayName("должен возвращать комментарий по id")
    @ParameterizedTest(name = "id = {0}")
    @MethodSource("commentIds")
    void shouldReturnCorrectCommentById(long id) {
        Comment entity = commentRepository.findById(id).orElseThrow();
        CommentDto expected = commentMapper.toDto(entity);

        Optional<CommentDto> actualOpt = commentService.findById(id);
        assertThat(actualOpt).isPresent();
        assertThat(actualOpt.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private static Stream<Long> commentIds() {
        return Stream.of(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @DisplayName("должен возвращать все комментарии для книги")
    @ParameterizedTest(name = "bookId = {0}")
    @MethodSource("commentsByBookId")
    void shouldReturnCommentsByBookId(long bookId, List<Long> expectedIds) {
        List<CommentDto> expected = expectedIds.stream()
                .map(id -> commentRepository.findById(id).orElseThrow())
                .map(commentMapper::toDto)
                .toList();

        List<CommentDto> actual = commentService.findAllByBookId(bookId);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(expected);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> commentsByBookId() {
        Map<Long, List<Long>> map = Map.of(
                1L, List.of(1L, 2L, 3L),
                2L, List.of(4L, 5L),
                3L, List.of(6L)
        );
        return map.entrySet().stream()
                .map(e -> org.junit.jupiter.params.provider.Arguments.arguments(e.getKey(), e.getValue()));
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldInsertNewComment() {
        CommentDto created = commentService.insert("New comment", 2L);
        CommentDto fetched = commentService.findById(created.id()).orElseThrow();
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @DisplayName("должен обновлять существующий комментарий")
    @Test
    void shouldUpdateExistingComment() {
        CommentDto updated = commentService.update(1L, 1L, "Modified content");

        Comment entity = commentRepository.findById(1L).orElseThrow();
        CommentDto expected = commentMapper.toDto(entity);

        assertThat(updated)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        assertThat(commentRepository.findById(3L)).isPresent();
        commentService.deleteById(3L, 1L);
        assertThat(commentRepository.findById(3L)).isEmpty();
    }

    @DisplayName("insert с несуществующей книгой должен бросать EntityNotFoundException")
    @Test
    void insertInvalidBookThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> commentService.insert("X", 999L));
    }

    @DisplayName("update для несуществующего комментария должен бросать EntityNotFoundException")
    @Test
    void updateNonExistingThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> commentService.update(999L, 1L, "X"));
    }
}
