package ru.gdemuzei.bot.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Конфигурация бинов Resilience4j.
 * Извлекает настроенные экземпляры из реестра для использования в сервисах.
 */
@Configuration
public class Resilience4jBeansConfig {

    private static final String SERVICE_NAME = "museumApi";

    /**
     * Создает бин CircuitBreaker для API музеев.
     */
    @Bean
    public CircuitBreaker museumCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker(SERVICE_NAME);
    }

    /**
     * Создает бин Retry для API музеев.
     */
    @Bean
    public Retry museumRetry(RetryRegistry registry) {
        return registry.retry(SERVICE_NAME);
    }

    /**
     * Создает бин RateLimiter для API музеев.
     */
    @Bean
    public RateLimiter museumRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter(SERVICE_NAME);
    }
}
