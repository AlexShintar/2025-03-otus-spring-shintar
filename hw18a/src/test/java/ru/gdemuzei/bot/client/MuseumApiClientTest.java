package ru.gdemuzei.bot.client;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.gdemuzei.bot.contracts.MuseumSummaryDto;
import ru.gdemuzei.bot.repositories.MuseumSearchCache;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MuseumApiClient + Resilience4j + Cache integration tests")
@SpringBootTest(
        classes = ru.gdemuzei.bot.BotApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "museum.api.base-url=http://localhost:8085",
                "bot.token=TEST_TOKEN",
                "spring.autoconfigure.exclude=org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration"
        }
)
@EnableWireMock(@ConfigureWireMock(port = 8085))
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MuseumApiClientTest {

    private static final String SEARCH_PATH = "/api/public/v2/museums/search";

    @Autowired
    private MuseumApiClient museumApiClient;

    @Autowired
    private MuseumSearchCache museumSearchCache;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("museumApi").reset();
        rateLimiterRegistry.rateLimiter("museumApi");
    }

    @Test
    @DisplayName("должен вернуть список музеев при здоровом внешнем API")
    void shouldReturnMuseumsWhenApiIsHealthy() {
        /*
           ѕровер€ем "Happy Path": внешний сервис доступен, возвращает 200 OK.
           ќжидаем получение корректного DTO.
        */
        stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("lat", equalTo("59.9"))
                .withQueryParam("lon", equalTo("30.3"))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "id": "1",
                                    "name": "Hermitage",
                                    "lat": 59.9,
                                    "lon": 30.3,
                                    "distanceKm": 0.5
                                  }
                                ]
                                """)));

        Flux<MuseumSummaryDto> result = museumApiClient.search(59.9, 30.3, 0, 10);

        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        "1".equals(dto.id()) &&
                                "Hermitage".equals(dto.name()))
                .verifyComplete();

        verify(1, getRequestedFor(urlPathEqualTo(SEARCH_PATH)));
    }

    @Test
    @DisplayName("должен выполнить ретраи при 500 и в итоге вернуть успех")
    void shouldRetryWhenApiReturns500ThenSucceed() {
        /*
           ѕровер€ем механизм Retry:
           1. Ёмулируем две ошибки 500 подр€д.
           2. Ќа третий раз возвращаем успех.
           ќжидаем, что клиент автоматически повторит запросы и вернет результат, не упав с ошибкой.
        */
        stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("lat", equalTo("55.0"))
                .withQueryParam("lon", equalTo("37.0"))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("10"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR))
                .willSetStateTo("Attempt 2"));

        stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("lat", equalTo("55.0"))
                .withQueryParam("lon", equalTo("37.0"))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("10"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Attempt 2")
                .willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR))
                .willSetStateTo("Success"));

        stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("lat", equalTo("55.0"))
                .withQueryParam("lon", equalTo("37.0"))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("10"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Success")
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "id": "2",
                                    "name": "Success Museum",
                                    "lat": 55.0,
                                    "lon": 37.0,
                                    "distanceKm": 1.0
                                  }
                                ]
                                """)));

        Flux<MuseumSummaryDto> result = museumApiClient.search(55.0, 37.0, 0, 10);

        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        "2".equals(dto.id()) &&
                                "Success Museum".equals(dto.name()))
                .verifyComplete();

        Retry retry = retryRegistry.retry("museumApi");
        assertThat(retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt())
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("должен использовать кеш как fallback, когда внешний сервис начинает падать")
    void shouldUseCacheAsFallbackWhenServiceFails() {
        /*
           ѕровер€ем Fallback и "Stale-while-revalidate":
           1. ƒелаем успешный запрос, чтобы данные сохранились в Caffeine.
           2. "Ћомаем" внешний сервис (всегда 500).
           3. ƒелаем повторный запрос.
           ќжидаем, что клиент перехватит ошибку и вернет данные, сохраненные на шаге 1.
         */
        stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("lat", equalTo("60.0"))
                .withQueryParam("lon", equalTo("30.0"))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("1"))
                .inScenario("Fallback Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "id": "99",
                                    "name": "Cached Museum",
                                    "lat": 60.0,
                                    "lon": 30.0,
                                    "distanceKm": 0.2
                                  }
                                ]
                                """))
                .willSetStateTo("FAIL"));

        stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("lat", equalTo("60.0"))
                .withQueryParam("lon", equalTo("30.0"))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("1"))
                .inScenario("Fallback Scenario")
                .whenScenarioStateIs("FAIL")
                .willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR)));

        Flux<MuseumSummaryDto> firstCall = museumApiClient.search(60.0, 30.0, 0, 1);
        MuseumSummaryDto[] firstResult = new MuseumSummaryDto[1];
        StepVerifier.create(firstCall)
                .assertNext(dto -> firstResult[0] = dto)
                .verifyComplete();

        Flux<MuseumSummaryDto> secondCall = museumApiClient.search(60.0, 30.0, 0, 1);
        StepVerifier.create(secondCall)
                .assertNext(dto -> {
                    assertThat(dto.id()).isEqualTo(firstResult[0].id());
                    assertThat(dto.name()).isEqualTo(firstResult[0].name());
                })
                .verifyComplete();
    }
}
