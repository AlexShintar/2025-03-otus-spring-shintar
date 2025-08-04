package ru.otus.hw.rest.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.services.AuthorService;

@Component
@RequiredArgsConstructor
public class AuthorHandler {
    private final AuthorService authorService;

    private final AuthorMapper authorMapper;

    public Mono<ServerResponse> getAllAuthors(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authorService.findAll().map(authorMapper::toDto), AuthorDto.class);
    }
}
