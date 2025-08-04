package ru.otus.hw.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.rest.handlers.GenreHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class GenreRestRouter {
    @Bean
    public RouterFunction<ServerResponse> genreRoutes(GenreHandler handler) {
        return RouterFunctions
                .route(GET("/api/v2/genre"), handler::getAllGenres);
    }
}
