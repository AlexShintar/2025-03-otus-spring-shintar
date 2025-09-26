package ru.otus.hw.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SearchRequest {
    private String id;

    private String queryText;

    private String source;

    private Instant timestamp;
}
