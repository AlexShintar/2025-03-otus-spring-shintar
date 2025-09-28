package ru.otus.hw.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.integration.executor")
public class IntegrationProperties {
    private int corePoolSize = 4;

    private int maxPoolSize = 8;

    private int queueCapacity = 64;

    private String threadNamePrefix = "integration-";

    private int awaitTerminationSeconds = 10;
}
