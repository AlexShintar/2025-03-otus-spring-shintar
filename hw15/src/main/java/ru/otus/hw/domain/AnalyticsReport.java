package ru.otus.hw.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AnalyticsReport {
    private Map<Intent, Long> countsByIntent;

    private List<String> top5Queries;
}
