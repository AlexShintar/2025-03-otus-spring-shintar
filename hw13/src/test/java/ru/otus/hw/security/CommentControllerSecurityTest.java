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

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asUser() {
        return user(TEST_USER);
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

    @DisplayName("POST /book/{bookId}/comment — аноним -> 302 login, user -> 302 /book/{id}")
    @ParameterizedTest
    @MethodSource("authOnlyData")
    void commentCreationSecurity(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                                 int expectedStatus,
                                 @Nullable String redirect) throws Exception {
        var req = MockMvcRequestBuilders.post("/book/{bookId}/comment", TEST_BOOK_ID)
                .param("content", TEST_COMMENT_CONTENT)
                .with(csrf());
        performAndAssert(req, principal, expectedStatus, redirect);
    }

    @DisplayName("PUT /book/{bookId}/comment/{commentId} — аноним -> 302 login, user -> 302 /book/{id}")
    @ParameterizedTest
    @MethodSource("authOnlyData")
    void commentUpdateSecurity(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                               int expectedStatus,
                               @Nullable String redirect) throws Exception {
        var req = MockMvcRequestBuilders.put("/book/{bookId}/comment/{commentId}", TEST_BOOK_ID, TEST_COMMENT_ID)
                .param("content", UPDATED_COMMENT_CONTENT)
                .with(csrf());
        performAndAssert(req, principal, expectedStatus, redirect);
    }

    @DisplayName("DELETE /book/{bookId}/comment/{commentId} — аноним -> 302 login, user -> 302 /book/{id}")
    @ParameterizedTest
    @MethodSource("authOnlyData")
    void commentDeletionSecurity(@Nullable SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal,
                                 int expectedStatus,
                                 @Nullable String redirect) throws Exception {
        var req = MockMvcRequestBuilders.delete("/book/{bookId}/comment/{commentId}", TEST_BOOK_ID, TEST_COMMENT_ID)
                .with(csrf());
        performAndAssert(req, principal, expectedStatus, redirect);
    }

    private static Stream<Arguments> authOnlyData() {
        return Stream.of(
                Arguments.of(null, 302, LOGIN_REDIRECT_URL),
                Arguments.of(asUser(), 302, SUCCESS_REDIRECT_URL)
        );
    }
}
