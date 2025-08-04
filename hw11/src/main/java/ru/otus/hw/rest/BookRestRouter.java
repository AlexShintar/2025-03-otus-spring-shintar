package ru.otus.hw.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.rest.handlers.BookHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;


@Configuration
public class BookRestRouter {
    @Bean
    public RouterFunction<ServerResponse> bookRoutes(BookHandler handler) {
        return RouterFunctions
                .route(GET("/api/v2/book"), handler::getAllBooks)
                .andRoute(GET("/api/v2/book/{id}"), handler::getBookById)
                .andRoute(POST("/api/v2/book"), handler::createBook)
                .andRoute(PUT("/api/v2/book/{id}"), handler::updateBook)
                .andRoute(DELETE("/api/v2/book/{id}"), handler::deleteBook);
    }
}
