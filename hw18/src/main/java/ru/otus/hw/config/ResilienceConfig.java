package ru.otus.hw.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Value("${external.book-recommendation-service.url}")
    private String externalServiceUrl;

    @Bean
    public RestClient externalRestClient() {
        var httpClient = HttpClients.custom()
                .disableAutomaticRetries()
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
