package ru.otus.hw.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(false);

        cacheManager.registerCustomCache("authorCache",
                buildCache(100, 30, TimeUnit.MINUTES));

        cacheManager.registerCustomCache("genreCache",
                buildCache(50, 60, TimeUnit.MINUTES));

        cacheManager.registerCustomCache("bookCache",
                buildCache(500, 30, TimeUnit.MINUTES));

        return cacheManager;
    }

    private Cache<Object, Object> buildCache(int maxSize, int ttl, TimeUnit timeUnit) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl, timeUnit)
                .build();
    }
}
