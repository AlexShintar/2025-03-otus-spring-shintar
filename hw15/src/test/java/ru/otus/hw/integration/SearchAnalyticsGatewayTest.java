package ru.otus.hw.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.domain.AnalyticsReport;
import ru.otus.hw.domain.Intent;
import ru.otus.hw.domain.SearchRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SearchAnalyticsGatewayTest {

    @Autowired
    private SearchAnalyticsGateway gateway;

    @DisplayName("Should classify, aggregate and return AnalyticsReport for a batch")
    @Test
    void shouldProcessBatch() {
        List<SearchRequest> batch = List.of(
                new SearchRequest("1", "59.9398,30.3146", "telegram", Instant.now()),
                new SearchRequest("2", "Pushkin museum", "web", Instant.now()),
                new SearchRequest("3", " ", "telegram", Instant.now()),
                new SearchRequest("4", "Tretyakov Gallery", "web", Instant.now()),
                new SearchRequest("5", "pushkin museum", "web", Instant.now())
        );

        AnalyticsReport report = gateway.process(batch);

        assertThat(report).isNotNull();

        assertThat(report.getCountsByIntent())
                .hasSize(3)
                .containsEntry(Intent.COORDS, 1L)
                .containsEntry(Intent.TEXT, 3L)
                .containsEntry(Intent.UNKNOWN, 1L);

        assertThat(report.getTop5Queries())
                .hasSize(3)
                .containsExactlyInAnyOrder("pushkin museum", "tretyakov gallery", "5993980 3031460");
    }
}
