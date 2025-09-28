package ru.otus.hw.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import ru.otus.hw.domain.AnalyticsReport;
import ru.otus.hw.domain.ClassifiedItem;
import ru.otus.hw.domain.Intent;
import ru.otus.hw.domain.SearchRequest;
import ru.otus.hw.util.GeoUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class IntegrationConfig {

    private final TaskExecutor integrationFlowTaskExecutor;

    /**
     * Проверяет, является ли строка валидными координатами, используя GeoUtil.
     */
    private static boolean looksLikeCoords(String s) {
        // Передаем null, так как нам нужен только булев результат, а не сами координаты
        return GeoUtil.parseLatLon(s, null);
    }

    /**
     * Нормализует строку:
     * - для координат - возвращает строку "lat lon" в формате целых чисел (умноженных на 100_000),
     * - для текста приводит к нижнему регистру, удаляет пунктуацию, сводит пробелы.
     */
    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        double[] coords = new double[2];
        if (GeoUtil.parseLatLon(s, coords)) {
            long latInt = Math.round(coords[0] * 100_000);
            long lonInt = Math.round(coords[1] * 100_000);
            return latInt + " " + lonInt;
        } else {
            return s.toLowerCase()
                    .trim()
                    .replaceAll("\\p{Punct}", "")
                    .replaceAll("\\s+", " ");
        }
    }

    @Bean
    public MessageChannel receiveSearchBatchChannel() {
        return MessageChannels.executor(integrationFlowTaskExecutor).getObject();
    }

    @Bean
    public MessageChannel returnReportChannel() {
        return MessageChannels.executor(integrationFlowTaskExecutor).getObject();
    }

    @Bean
    public MessageChannel geoSubflowChannel() {
        return MessageChannels.executor(integrationFlowTaskExecutor).getObject();
    }

    @Bean
    public MessageChannel textSubflowChannel() {
        return MessageChannels.executor(integrationFlowTaskExecutor).getObject();
    }

    @Bean
    public MessageChannel classifiedChannel() {
        return MessageChannels.executor(integrationFlowTaskExecutor).getObject();
    }

    @Bean(name = "errorChannel")
    public MessageChannel errorChannel() {
        return MessageChannels.publishSubscribe().getObject();
    }

    @Bean
    public IntegrationFlow searchAnalyticsFlow() {
        return IntegrationFlow.from(receiveSearchBatchChannel())
                .log(LoggingHandler.Level.INFO, "flow", m -> "Stage 0: received batch")
                .split()
                .<SearchRequest, Boolean>route(
                        sr -> looksLikeCoords(sr.getQueryText()),
                        r -> r
                                .channelMapping(true, "geoSubflowChannel")
                                .channelMapping(false, "textSubflowChannel")
                )
                .get();
    }

    @Bean
    public IntegrationFlow geoSubflow() {
        return IntegrationFlow.from(geoSubflowChannel())
                .log(LoggingHandler.Level.INFO, "flow", m -> "Stage 1.GEO: classify as COORDS")
                .<SearchRequest, ClassifiedItem>transform(sr ->
                        new ClassifiedItem(
                                sr.getId(),
                                Intent.COORDS,
                                normalize(sr.getQueryText()),
                                sr.getSource()
                        )
                )
                .channel("classifiedChannel")
                .get();
    }

    @Bean
    public IntegrationFlow textSubflow() {
        return IntegrationFlow.from(textSubflowChannel())
                .log(LoggingHandler.Level.INFO, "flow", m -> "Stage 1.TEXT: normalize & classify")
                .<SearchRequest, ClassifiedItem>transform(sr -> {
                    String norm = normalize(sr.getQueryText());
                    Intent intent = StringUtils.isBlank(norm) ? Intent.UNKNOWN : Intent.TEXT;
                    return new ClassifiedItem(sr.getId(), intent, norm, sr.getSource());
                })
                .channel("classifiedChannel")
                .get();
    }

    @Bean
    public IntegrationFlow aggregateAndReportFlow() {
        return IntegrationFlow.from(classifiedChannel())
                .aggregate(a -> a
                        .groupTimeout(10_000) // таймаут в миллисекундах
                        .expireGroupsUponCompletion(true)
                )
                .log(LoggingHandler.Level.INFO, "flow", m -> "Stage 3: aggregated (full)")
                .transform(this::toReport)
                .channel(returnReportChannel())
                .get();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlow.from("errorChannel")
                .handle(message -> {
                    Object payload = message.getPayload();
                    if (payload instanceof MessagingException ex) {
                        var failed = ex.getFailedMessage();
                        log.error("INTEGRATION ERROR: {}, failedHeaders={}, failedPayload={}",
                                ex,
                                failed != null ? failed.getHeaders() : "n/a",
                                failed != null ? failed.getPayload() : "n/a",
                                ex);
                    } else {
                        log.error("INTEGRATION ERROR: {}", payload);
                    }
                })
                .get();
    }

    private AnalyticsReport toReport(List<ClassifiedItem> items) {
        Map<Intent, Long> counts = items.stream()
                .collect(Collectors.groupingBy(ClassifiedItem::getIntent, Collectors.counting()));

        List<String> top5 = items.stream()
                .map(ClassifiedItem::getNormalizedQuery)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        return new AnalyticsReport(counts, top5);
    }
}
