package ru.otus.hw.services;

import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.otus.hw.dto.BookRecommendationDto;

import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalBookRecommendationService {

    private final RestClient externalRestClient;

    private final CircuitBreaker circuitBreaker;

    private final Retry retry;

    public BookRecommendationDto getRecommendation(Long bookId) {
        log.info("Fetching recommendation for bookId: {}", bookId);

        Supplier<BookRecommendationDto> supplier = () -> callExternalService(bookId);

        Supplier<BookRecommendationDto> retried = io.github.resilience4j.retry.Retry
                .decorateSupplier(retry, supplier);


        return circuitBreaker.run(
                retried,
                t -> {
                    log.error("Fallback for bookId {}: {}", bookId, t.toString());
                    return new BookRecommendationDto(bookId, "No recommendation available", 0.0);
                }
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
