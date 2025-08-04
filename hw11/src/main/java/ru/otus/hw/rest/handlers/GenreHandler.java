package ru.otus.hw.rest.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.dto.GenreDto;

@Component
@RequiredArgsConstructor
public class GenreHandler {
    private final GenreService genreService;

    private final GenreMapper genreMapper;

    public Mono<ServerResponse> getAllGenres(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(genreService.findAll().map(genreMapper::toDto), GenreDto.class);
    }
}
