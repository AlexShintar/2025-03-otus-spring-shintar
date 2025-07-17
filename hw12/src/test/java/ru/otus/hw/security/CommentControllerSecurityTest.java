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
import ru.otus.hw.controllers.CommentController;
import ru.otus.hw.services.CommentService;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(controllers = CommentController.class)
class CommentControllerSecurityTest {

    private static final String LOGIN_REDIRECT_URL = "http://localhost/login";
    private static final String SUCCESS_REDIRECT_URL = "/book/1";
    private static final String TEST_USER = "testuser";

    private static final Long TEST_BOOK_ID = 1L;
    private static final Long TEST_COMMENT_ID = 1L;
    private static final String TEST_COMMENT_CONTENT = "Comment_1_for_BookTitle_1";
    private static final String UPDATED_COMMENT_CONTENT = "Updated comment content";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

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

    @DisplayName("Тест безопасности при создании комментария")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("modifyingRequestsTestData")
    void testCommentCreationSecurity(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/book/{bookId}/comment", TEST_BOOK_ID)
                .param("content", TEST_COMMENT_CONTENT)
                .with(csrf());
        executeRequestAndVerify(requestBuilder, userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест безопасности при обновлении комментария")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("modifyingRequestsTestData")
    void testCommentUpdateSecurity(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.put("/book/{bookId}/comment/{commentId}", TEST_BOOK_ID, TEST_COMMENT_ID)
                .param("content", UPDATED_COMMENT_CONTENT)
                .with(csrf());
        executeRequestAndVerify(requestBuilder, userName, expectedStatus, expectedRedirectUrl);
    }

    @DisplayName("Тест безопасности при удалении комментария")
    @ParameterizedTest(name = "Пользователь: {0} -> HTTP статус: {1}")
    @MethodSource("modifyingRequestsTestData")
    void testCommentDeletionSecurity(@Nullable String userName, int expectedStatus, @Nullable String expectedRedirectUrl) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/book/{bookId}/comment/{commentId}", TEST_BOOK_ID, TEST_COMMENT_ID)
                .with(csrf());
        executeRequestAndVerify(requestBuilder, userName, expectedStatus, expectedRedirectUrl);
    }

    private static Stream<Arguments> modifyingRequestsTestData() {
        return Stream.of(
                Arguments.of(TEST_USER, 302, SUCCESS_REDIRECT_URL),
                Arguments.of(null, 302, LOGIN_REDIRECT_URL)
        );
    }
}
