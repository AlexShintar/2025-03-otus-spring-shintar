package ru.otus.hw.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassifiedItem {
    private String id;

    private Intent intent;

    private String normalizedQuery;

    private String source;
}
