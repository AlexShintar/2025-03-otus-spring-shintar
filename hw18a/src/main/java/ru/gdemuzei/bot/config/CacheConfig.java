package ru.gdemuzei.bot.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gdemuzei.bot.contracts.MuseumSummaryDto;

import java.time.Duration;
import java.util.List;

/**
 * Конфигурация кешей Caffeine.
 * Настраивает политики вытеснения и время жизни записей.
 */
@Slf4j
@Configuration
public class CacheConfig {

    /**
     * Кеш для хранения детальной информации по конкретному музею.
     */
    @Bean
    public Cache<String, MuseumSummaryDto> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(1500)
                .expireAfterAccess(Duration.ofDays(7))
                .scheduler(Scheduler.systemScheduler())
                .build();
    }

    /**
     * Кеш для хранения списков результатов поиска.
     */
    @Bean
    public Cache<String, List<MuseumSummaryDto>> searchResultsCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10))
                .scheduler(Scheduler.systemScheduler())
                .build();
    }
}
