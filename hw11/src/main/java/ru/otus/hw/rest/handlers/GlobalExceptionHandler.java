package ru.otus.hw.rest.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<Rendering> handleValidationException(WebExchangeBindException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> String.format("Field '%s': %s", err.getField(), err.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return Mono.just(
                Rendering.view("error")
                        .modelAttribute("message", "Validation failed: " + errorMessage)
                        .status(HttpStatus.BAD_REQUEST)
                        .build()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<Rendering> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return Mono.just(
                Rendering.view("error")
                        .modelAttribute("message", ex.getMessage())
                        .status(HttpStatus.NOT_FOUND)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<Rendering> handleAny(Exception ex, ServerWebExchange exchange) {
        String errorId = UUID.randomUUID().toString();
        log.error("Internal error at {}. ErrorId={}", exchange.getRequest().getURI(), errorId, ex);
        return Mono.just(
                Rendering.view("error")
                        .modelAttribute("message", "Internal server error. Please try again later.")
                        .modelAttribute("errorId", errorId)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build()
        );
    }
}
