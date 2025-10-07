package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import ru.otus.hw.dto.BookRecommendationDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("Сервис для получения рекомендаций")
class ExternalBookRecommendationServiceTest {

    @Autowired
    private ExternalBookRecommendationService service;

    @MockitoBean
    private RestClient externalRestClient;

    @MockitoBean
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @MockitoBean
    private RestClient.ResponseSpec responseSpec;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        reset(externalRestClient, requestHeadersUriSpec, responseSpec);
        circuitBreakerRegistry.circuitBreaker("externalServiceCB").reset();
    }

    @DisplayName("должен успешно получить рекомендацию")
    @Test
    void shouldReturnRecommendationOnSuccess() throws Exception {
        Long bookId = 1L;
        BookRecommendationDto expected = new BookRecommendationDto(1L, "Great Book", 4.8);

        when(externalRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), (Object) any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BookRecommendationDto.class)).thenReturn(expected);

        CompletableFuture<BookRecommendationDto> result = service.getRecommendation(bookId);

        BookRecommendationDto dto = result.get(10, TimeUnit.SECONDS);

        assertThat(dto).isNotNull();
        assertThat(dto.bookId()).isEqualTo(bookId);
        assertThat(dto.recommendedTitle()).isEqualTo("Great Book");
        assertThat(dto.rating()).isEqualTo(4.8);

        verify(externalRestClient, times(1)).get();
    }

    @DisplayName("должен выполнить retry при ошибке и вернуть fallback")
    @Test
    void shouldRetryAndReturnFallbackOnError() throws Exception {
        Long bookId = 2L;

        when(externalRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), (Object) any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BookRecommendationDto.class))
                .thenThrow(new RuntimeException("Service unavailable"));

        CompletableFuture<BookRecommendationDto> result = service.getRecommendation(bookId);

        BookRecommendationDto dto = result.get(10, TimeUnit.SECONDS);

        assertThat(dto).isNotNull();
        assertThat(dto.bookId()).isEqualTo(bookId);
        assertThat(dto.recommendedTitle()).isEqualTo("No recommendation available");
        assertThat(dto.rating()).isEqualTo(0.0);

        verify(externalRestClient, times(3)).get();
    }

    @DisplayName("должен открыть circuit breaker после серии ошибок")
    @Test
    void shouldOpenCircuitBreakerAfterFailures() throws Exception {
        Long bookId = 3L;

        when(externalRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), (Object) any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BookRecommendationDto.class))
                .thenThrow(new RuntimeException("Service unavailable"));

        IntStream.rangeClosed(1, 5).forEach(i -> {
            try {
                service.getRecommendation(bookId).get(10, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        });

        int callsBeforeNextAttempt = mockingDetails(externalRestClient).getInvocations().size();

        CompletableFuture<BookRecommendationDto> result = service.getRecommendation(bookId);
        BookRecommendationDto dto = result.get(10, TimeUnit.SECONDS);

        assertThat(dto).isNotNull();
        assertThat(dto.recommendedTitle()).isEqualTo("No recommendation available");

        int callsAfterNextAttempt = mockingDetails(externalRestClient).getInvocations().size();

        assertThat(callsAfterNextAttempt).isEqualTo(callsBeforeNextAttempt);
    }
}
