package ru.otus.hw.services;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.web.client.RestClient;
import ru.otus.hw.dto.BookRecommendationDto;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Сервис для получения рекомендаций")
@ExtendWith(MockitoExtension.class)
class ExternalBookRecommendationServiceTest {

    @Mock
    private RestClient externalRestClient;

    @Mock
    private CircuitBreaker circuitBreaker;

    private Retry retry;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ExternalBookRecommendationService service;

    @BeforeEach
    void setUp() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .build();
        retry = Retry.of("test", config);

        service = new ExternalBookRecommendationService(
                externalRestClient, circuitBreaker, retry);
    }

    @DisplayName("должен вернуть рекомендацию при успешном вызове")
    @Test
    void shouldReturnRecommendationOnSuccess() {
        Long bookId = 1L;
        BookRecommendationDto expected = new BookRecommendationDto(1L, "Great Book", 4.8);

        when(externalRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), (Object) any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BookRecommendationDto.class)).thenReturn(expected);

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<BookRecommendationDto> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        BookRecommendationDto result = service.getRecommendation(bookId);

        assertThat(result).isNotNull();
        assertThat(result.bookId()).isEqualTo(bookId);
        assertThat(result.recommendedTitle()).isEqualTo("Great Book");
        assertThat(result.rating()).isEqualTo(4.8);
    }

    @DisplayName("должен вернуть fallback при ошибке")
    @Test
    void shouldReturnFallbackOnError() {
        Long bookId = 1L;

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Function<Throwable, BookRecommendationDto> fallback = invocation.getArgument(1);
                    return fallback.apply(new RuntimeException("Service unavailable"));
                });

        BookRecommendationDto result = service.getRecommendation(bookId);

        assertThat(result).isNotNull();
        assertThat(result.bookId()).isEqualTo(bookId);
        assertThat(result.recommendedTitle()).isEqualTo("No recommendation available");
        assertThat(result.rating()).isEqualTo(0.0);
    }
}
