package ru.otus.hw.rest.handlers;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

import java.net.URI;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@Component
@RequiredArgsConstructor
public class BookHandler {

    private final BookService bookService;

    private final Validator validator;

    private <T> Mono<T> validate(T dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return Mono.just(dto);
    }

    public Mono<ServerResponse> getAllBooks(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(bookService.findAll(), BookDto.class));
    }

    public Mono<ServerResponse> getBookById(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookService.findById(id)
                .flatMap(bookDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(bookDto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createBook(ServerRequest request) {
        return request.bodyToMono(BookCreateDto.class)
                .flatMap(this::validate)                                .flatMap(bookService::insert)
                .flatMap(created -> ServerResponse
                        .created(URI.create("/api/v2/book/" + created.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(created));
    }

    public Mono<ServerResponse> updateBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(BookUpdateDto.class)
                .flatMap(this::validate)
                .flatMap(dto -> bookService.update(dto, id))
                .flatMap(updated -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updated))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookService.deleteById(id)
                .then(ServerResponse.noContent().build());
    }
}
