package ru.otus.hw.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.rest.handlers.CommentHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;

@Configuration
public class CommentRestRouter {
    @Bean
    public RouterFunction<ServerResponse> commentRoutes(CommentHandler handler) {
        return RouterFunctions
                .route(GET("/api/v2/book/{bookId}/comment"), handler::getComments)
                .andRoute(POST("/api/v2/book/{bookId}/comment"), handler::addComment)
                .andRoute(PUT("/api/v2/book/{bookId}/comment/{commentId}"), handler::updateComment)
                .andRoute(DELETE("/api/v2/book/{bookId}/comment/{commentId}"), handler::deleteComment);
    }
}
