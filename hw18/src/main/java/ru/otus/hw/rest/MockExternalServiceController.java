package ru.otus.hw.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookRecommendationDto;

import java.util.Random;

@Slf4j
@RestController
public class MockExternalServiceController {

    private final Random random = new Random();

    @GetMapping("/api/recommendations/{id}")
    public BookRecommendationDto getRecommendation(@PathVariable Long id) {
        // Эмуляция задержек/ошибок
        if (random.nextInt(100) < 40) {
            delay();
        }
        if (random.nextInt(100) < 30) {
            throw new RuntimeException("External service error!");
        }
        return new BookRecommendationDto(id, "Recommended Book #" + id, 4.5);
    }

    private void delay() {
        int ms = random.nextInt(1000, 3000);
        log.info("Simulated delay: {} ms", ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
