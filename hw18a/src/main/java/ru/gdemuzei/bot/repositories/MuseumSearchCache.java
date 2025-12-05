package ru.gdemuzei.bot.repositories;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.gdemuzei.bot.contracts.MuseumSummaryDto;

import java.util.List;

/**
 * Компонент для управления кешированием результатов поиска.
 * Использует Caffeine для хранения полных страниц выдачи.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MuseumSearchCache {

    private final Cache<String, List<MuseumSummaryDto>> searchResultsCache;

    /**
     * Сохраняет список музеев в кеш.
     * Перезаписывает данные только если передан непустой список.
     *
     * @param key Ключ кеширования
     * @param dtos Список найденных музеев
     */
    public void put(String key, List<MuseumSummaryDto> dtos) {
        if (dtos != null && !dtos.isEmpty()) {
            searchResultsCache.put(key, dtos);
            log.debug("Cached {} items for key: {}", dtos.size(), key);
        }
    }

    /**
     * Возвращает данные из кеша в виде реактивного потока.
     *
     * @param key Ключ кеширования
     * @return Flux с музеями или пустой Flux, если данных нет
     */
    public Flux<MuseumSummaryDto> get(String key) {
        List<MuseumSummaryDto> cached = searchResultsCache.getIfPresent(key);
        if (cached != null && !cached.isEmpty()) {
            log.info("Fallback: Returning {} cached results for key: {}", cached.size(), key);
            return Flux.fromIterable(cached);
        }
        log.warn("Fallback: No cached data found for key: {}", key);
        return Flux.empty();
    }
}
