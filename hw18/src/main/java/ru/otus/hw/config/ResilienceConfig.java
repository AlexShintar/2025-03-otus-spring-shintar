package ru.otus.hw.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Value("${external.book-recommendation-service.url}")
    private String externalServiceUrl;

    /** CircuitBreaker из Spring Cloud (обёртка) */
    @Bean
    public CircuitBreaker circuitBreaker(CircuitBreakerFactory<?, ?> factory) {
        return factory.create("externalServiceCB");
    }

    /** Retry из Resilience4j */
    @Bean
    public Retry retry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("externalServiceRetry");
    }

    /** (Опционально) привязка TimeLimiter к конкретному CB имени */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> cbCustomizer(
            TimeLimiterRegistry tlRegistry,
            CircuitBreakerRegistry cbRegistry) {
        return factory -> factory.configure(builder ->
                        new Resilience4JConfigBuilder("externalServiceCB")
                                .timeLimiterConfig(tlRegistry.timeLimiter("externalServiceTL")
                                        .getTimeLimiterConfig())
                                .circuitBreakerConfig(cbRegistry.circuitBreaker("externalServiceCB")
                                        .getCircuitBreakerConfig())
                                .build(),
                "externalServiceCB");
    }

    /** RestClient с реальными сетевыми таймаутами (обязательно!) */
    @Bean
    public RestClient externalRestClient() {
        var httpClient = HttpClients.custom()
                .disableAutomaticRetries() // ретраи делаем сами через Resilience4j
                .build();

        var rf = new HttpComponentsClientHttpRequestFactory(httpClient);
        rf.setConnectTimeout(Duration.ofMillis(300));
        rf.setConnectionRequestTimeout(Duration.ofMillis(300));
        rf.setReadTimeout(Duration.ofMillis(2000));

        return RestClient.builder()
                .requestFactory(rf)
                .baseUrl(externalServiceUrl)
                .build();
    }
}
