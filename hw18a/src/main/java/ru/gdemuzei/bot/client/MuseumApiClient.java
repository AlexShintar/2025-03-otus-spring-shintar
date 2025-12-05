package ru.gdemuzei.bot.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.gdemuzei.bot.config.MuseumApiConfig;
import ru.gdemuzei.bot.contracts.MuseumSummaryDto;
import ru.gdemuzei.bot.repositories.MuseumSearchCache;

/**
 * Клиент для взаимодействия с внешним API музеев.
 * Реализует паттерны отказоустойчивости Resilience4j и кеширование результатов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MuseumApiClient {

    private final WebClient webClient;

    private final MuseumApiConfig apiConfig;

    private final MuseumSearchCache searchCache;

    private final CircuitBreaker museumCircuitBreaker;

    private final Retry museumRetry;

    private final RateLimiter museumRateLimiter;

    /**
     * Выполняет поиск музеев по географическим координатам.
     * При сбое внешнего сервиса возвращает сохраненные данные из кеша.
     *
     * @param lat Широта
     * @param lon Долгота
     * @param offset Смещение выборки
     * @param limit Количество элементов
     * @return Поток объектов DTO с информацией о музеях
     */
    public Flux<MuseumSummaryDto> search(double lat, double lon, int offset, int limit) {
        String cacheKey = String.format("geo:%.6f:%.6f:%d:%d", lat, lon, offset, limit);

        return webClient.get()
                .uri(uri -> uri
                        .path(apiConfig.searchPath())
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("offset", offset)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToFlux(MuseumSummaryDto.class)
                .transformDeferred(RateLimiterOperator.of(museumRateLimiter))
                .transformDeferred(CircuitBreakerOperator.of(museumCircuitBreaker))
                .transformDeferred(RetryOperator.of(museumRetry))
                .collectList()
                .doOnNext(list -> searchCache.put(cacheKey, list))
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(throwable -> {
                    log.warn("Museum search failed: [{}] {}. Returning cached data.",
                            throwable.getClass().getSimpleName(), throwable.getMessage());
                    return searchCache.get(cacheKey);
                });
    }

    /**
     * Выполняет текстовый поиск музеев.
     * При сбое внешнего сервиса возвращает сохраненные данные из кеша.
     *
     * @param query Текст запроса
     * @param offset Смещение выборки
     * @param limit Количество элементов
     * @return Поток объектов DTO с информацией о музеях
     */
    public Flux<MuseumSummaryDto> searchText(String query, int offset, int limit) {
        String cacheKey = String.format("text:%s:%d:%d", query, offset, limit);

        return webClient.get()
                .uri(uri -> uri
                        .path(apiConfig.searchPath())
                        .queryParam("q", query)
                        .queryParam("offset", offset)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToFlux(MuseumSummaryDto.class)
                .transformDeferred(RateLimiterOperator.of(museumRateLimiter))
                .transformDeferred(CircuitBreakerOperator.of(museumCircuitBreaker))
                .transformDeferred(RetryOperator.of(museumRetry))
                .collectList()
                .doOnNext(list -> searchCache.put(cacheKey, list))
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(throwable -> {
                    log.warn("Text search failed: [{}] {}. Returning cached data.",
                            throwable.getClass().getSimpleName(), throwable.getMessage());
                    return searchCache.get(cacheKey);
                });
    }
}
