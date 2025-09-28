package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.repositories.CommentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Тестирование правил доступа ACL для сервиса комментариев")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentServiceAclTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    private static final long ADMIN_COMMENT_ID = 3L;
    private static final long USER_COMMENT_ID = 4L;
    private static final long BOOK_ID_WITH_MIXED_COMMENTS = 2L;

    @Test
    @DisplayName("Администратор может обновить комментарий другого пользователя")
    @WithMockUser(username = "vetinari", roles = "ADMIN")
    void adminCanUpdateAnotherUsersComment() {
        String newContent = "Административное изменение";

        CommentDto updatedComment = assertDoesNotThrow(
                () -> commentService.update(USER_COMMENT_ID, newContent)
        );

        assertThat(updatedComment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("Администратор может удалить комментарий другого пользователя")
    @WithMockUser(username = "vetinari", roles = "ADMIN")
    void adminCanDeleteAnotherUsersComment() {
        assertThat(commentRepository.findById(USER_COMMENT_ID)).isPresent();

        assertDoesNotThrow(() -> commentService.deleteById(USER_COMMENT_ID));

        assertThat(commentRepository.findById(USER_COMMENT_ID)).isEmpty();
    }

    @Test
    @DisplayName("Пользователь может обновить свой собственный комментарий")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCanUpdateOwnComment() {
        String newContent = "Я, Шнобби Шноббс, обновил свое сообщение";

        CommentDto updatedComment = assertDoesNotThrow(
                () -> commentService.update(USER_COMMENT_ID, newContent)
        );

        assertThat(updatedComment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("Пользователь может удалить свой собственный комментарий")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCanDeleteOwnComment() {
        assertThat(commentRepository.findById(USER_COMMENT_ID)).isPresent();

        assertDoesNotThrow(() -> commentService.deleteById(USER_COMMENT_ID));

        assertThat(commentRepository.findById(USER_COMMENT_ID)).isEmpty();
    }

    @Test
    @DisplayName("Пользователь НЕ может обновить комментарий администратора)")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCannotUpdateAnotherUsersComment() {
        assertThrows(AccessDeniedException.class, () -> commentService.update(ADMIN_COMMENT_ID, "Попытка незаконного изменения"));
    }

    @Test
    @DisplayName("Пользователь НЕ может удалить комментарий администратора")
    @WithMockUser(username = "nobby", roles = "USER")
    void userCannotDeleteAnotherUsersComment() {
        assertThrows(AccessDeniedException.class, () -> commentService.deleteById(ADMIN_COMMENT_ID));
    }

    @Test
    @DisplayName("@PostFilter должен вернуть все комментарии для книги пользователю с ролью USER")
    @WithMockUser(username = "nobby", roles = "USER")
    void findAllByBookIdReturnsAllCommentsForUserWithReadPermission() {
        var comments = commentService.findAllByBookId(BOOK_ID_WITH_MIXED_COMMENTS);
        assertThat(comments)
                .hasSize(1)
                .extracting(CommentDto::getId)
                .containsExactlyInAnyOrder(USER_COMMENT_ID);
    }

    @Test
    @DisplayName("При создании комментария пользователем корректно создаются ACL записи")
    @WithMockUser(username = "nobby", roles = "USER")
    @Transactional
    void insertingCommentCreatesCorrectAclForOwner() {
        CommentDto created = commentService.insert("Это мой новый комментарий, я его владелец", 3L);
        long newCommentId = created.getId();

        CommentDto updated = assertDoesNotThrow(
                () -> commentService.update(newCommentId, "И я могу его поменять"),
                "Владелец должен иметь возможность редактировать свой комментарий"
        );
        assertThat(updated.getContent()).isEqualTo("И я могу его поменять");

        assertDoesNotThrow(
                () -> commentService.deleteById(newCommentId),
                "Владелец должен иметь возможность удалить свой комментарий"
        );

        assertThat(commentRepository.findById(newCommentId)).isEmpty();
    }
}
