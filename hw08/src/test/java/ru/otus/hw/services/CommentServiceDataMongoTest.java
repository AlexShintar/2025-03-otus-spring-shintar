package ru.otus.hw.services;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.testchangelog.TestDatabaseChangelog;
import ru.otus.hw.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Сервис для работы с комментариями (DataMongoTest)")
@DataMongoTest
@EnableMongock
@Import({
        TestDatabaseChangelog.class,
        CommentServiceImpl.class,
        BookServiceImpl.class,
        CommentConverter.class,
        BookConverter.class,
        AuthorConverter.class,
        GenreConverter.class
})
class CommentServiceDataMongoTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentConverter commentConverter;

    private static final List<Comment> initialComments = TestDataFactory.comments();
    private static final List<String> commentIds = initialComments.stream()
            .map(Comment::getId)
            .toList();
    private static final List<String> bookIds = TestDataFactory.books().stream()
            .map(Book::getId)
            .distinct()
            .toList();
    private static final List<String> bookTitles = TestDataFactory.books().stream()
            .map(Book::getTitle)
            .distinct()
            .toList();

    @DisplayName("должен возвращать комментарий по id для всех предзагруженных")
    @ParameterizedTest(name = "id = {0}")
    @MethodSource("commentIdsProvider")
    void shouldReturnCorrectCommentById(String id) {
        Comment expectedEntity = initialComments.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow();
        CommentDto expected = commentConverter.toDto(expectedEntity);

        Optional<CommentDto> actualOpt = commentService.findById(id);
        assertThat(actualOpt)
                .as("Comment %s should be found", id)
                .isPresent();
        assertThat(actualOpt.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    static Stream<String> commentIdsProvider() {
        return commentIds.stream();
    }

    @DisplayName("должен возвращать все комментарии для книги")
    @ParameterizedTest(name = "bookId = {0}")
    @MethodSource("commentsByBookIdProvider")
    void shouldReturnCommentsByBookId(String bookId, List<String> expectedIds) {
        List<CommentDto> expected = initialComments.stream()
                .filter(c -> c.getBook().getId().equals(bookId))
                .filter(c -> expectedIds.contains(c.getId()))
                .map(commentConverter::toDto)
                .toList();

        List<CommentDto> actual = commentService.findAllByBookId(bookId);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(expected);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> commentsByBookIdProvider() {
        return bookIds.stream()
                .map(id -> org.junit.jupiter.params.provider.Arguments.arguments(
                        id,
                        initialComments.stream()
                                .filter(c -> c.getBook().getId().equals(id))
                                .map(Comment::getId)
                                .collect(Collectors.toList())
                ));
    }


    @DisplayName("bookId в CommentDto должен ссылаться на существующую книгу")
    @Test
    void commentBookIdShouldPointToExistingBook() {
        List<CommentDto> allComments = bookIds.stream()
                .flatMap(id -> commentService.findAllByBookId(id).stream())
                .collect(Collectors.toList());

        assertThat(allComments).isNotEmpty();
        allComments.forEach(c ->
                assertThat(c.book()).isIn(bookTitles)
        );
    }

    @DirtiesContext
    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldInsertNewComment() {
        String bookId = bookIds.get(1);
        CommentDto created = commentService.insert("New comment", bookId);
        CommentDto fetched = commentService.findById(created.id()).orElseThrow();
        assertThat(fetched)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @DirtiesContext
    @DisplayName("должен обновлять существующий комментарий")
    @Test
    void shouldUpdateExistingComment() {
        String id = commentIds.get(0);
        CommentDto updated = commentService.update(id, "Modified content");

        CommentDto fetched = commentService.findById(id).orElseThrow();
        assertThat(updated)
                .usingRecursiveComparison()
                .isEqualTo(fetched);
    }

    @DirtiesContext
    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        String id = commentIds.get(2);
        assertThat(commentService.findById(id)).isPresent();
        commentService.deleteById(id);
        assertThat(commentService.findById(id)).isEmpty();
    }

    @DisplayName("insert с несуществующей книгой должен бросать EntityNotFoundException")
    @Test
    void insertInvalidBookThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> commentService.insert("X", "invalid-id")
        );
    }

    @DisplayName("update для несуществующего комментария должен бросать EntityNotFoundException")
    @Test
    void updateNonExistingThrows() {
        assertThrows(EntityNotFoundException.class,
                () -> commentService.update("invalid-id", "X")
        );
    }
}
