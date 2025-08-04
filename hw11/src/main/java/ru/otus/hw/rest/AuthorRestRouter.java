package ru.otus.hw.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.rest.handlers.AuthorHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class AuthorRestRouter {
    @Bean
    public RouterFunction<ServerResponse> authorRoutes(AuthorHandler handler) {
        return RouterFunctions
                .route(GET("/api/v2/author"), handler::getAllAuthors);
    }
}
