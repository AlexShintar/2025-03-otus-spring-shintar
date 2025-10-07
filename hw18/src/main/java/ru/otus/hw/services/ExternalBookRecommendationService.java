package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.otus.hw.dto.BookRecommendationDto;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalBookRecommendationService {

    private final RestClient externalRestClient;

    @Retry(name = "externalServiceRetry", fallbackMethod = "fallbackMethod")
    @CircuitBreaker(name = "externalServiceCB")  // БЕЗ fallback!
    public CompletableFuture<BookRecommendationDto> getRecommendation(Long bookId) {
        log.info("Fetching recommendation for bookId: {}", bookId);
        BookRecommendationDto result = callExternalService(bookId);
        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<BookRecommendationDto> fallbackMethod(Long bookId, Throwable t) {
        log.error("Fallback for bookId {}: {}", bookId, t.getMessage());
        return CompletableFuture.completedFuture(
                new BookRecommendationDto(bookId, "No recommendation available", 0.0)
        );
    }

    private BookRecommendationDto callExternalService(Long bookId) {
        log.info("Calling external recommendation service for bookId: {}", bookId);
        return externalRestClient.get()
                .uri("/api/recommendations/{id}", bookId)
                .retrieve()
                .body(BookRecommendationDto.class);
    }
}
