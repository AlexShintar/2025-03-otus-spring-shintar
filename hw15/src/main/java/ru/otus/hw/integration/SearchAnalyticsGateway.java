package ru.otus.hw.integration;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.domain.AnalyticsReport;
import ru.otus.hw.domain.SearchRequest;

import java.util.List;

@MessagingGateway
public interface SearchAnalyticsGateway {

    @Gateway(requestChannel = "receiveSearchBatchChannel", replyChannel = "returnReportChannel")
    AnalyticsReport process(List<SearchRequest> batch);
}
