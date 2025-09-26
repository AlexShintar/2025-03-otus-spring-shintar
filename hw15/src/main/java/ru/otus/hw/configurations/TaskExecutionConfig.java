package ru.otus.hw.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
@RequiredArgsConstructor
public class TaskExecutionConfig {

    private final IntegrationProperties props;

    @Bean
    public TaskExecutor integrationFlowTaskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(props.getCorePoolSize());
        ex.setMaxPoolSize(props.getMaxPoolSize());
        ex.setQueueCapacity(props.getQueueCapacity());
        ex.setThreadNamePrefix(props.getThreadNamePrefix());
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(props.getAwaitTerminationSeconds());
        ex.initialize();
        return ex;
    }
}
