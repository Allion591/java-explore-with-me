package ru.practicum.stats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.StatsServerStart;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = StatsServerStart.class)
@ActiveProfiles("test")
@Transactional
class StatsRepositoryIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private StatsRepository statsRepository;

    private Hit hit1, hit2, hit3, hit4;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        entityManager.createQuery("DELETE FROM Hit").executeUpdate();

        // Создаем тестовые данные
        hit1 = Hit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        hit2 = Hit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.2")
                .timestamp(LocalDateTime.of(2024, 1, 1, 11, 0))
                .build();

        hit3 = Hit.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.1") // Тот же IP для проверки уникальности
                .timestamp(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        hit4 = Hit.builder()
                .app("another-service")
                .uri("/events/1")
                .ip("192.168.1.3")
                .timestamp(LocalDateTime.of(2024, 1, 1, 13, 0))
                .build();

        // Сохраняем
        entityManager.persist(hit1);
        entityManager.persist(hit2);
        entityManager.persist(hit3);
        entityManager.persist(hit4);
        entityManager.flush();
    }

    @Test
    void getStats_shouldReturnAllHitsWhenNoFilters() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        List<ViewStats> stats = statsRepository.getStats(start, end, null, false);

        assertEquals(3, stats.size());

        assertTrue(stats.get(0).getHits() >= stats.get(1).getHits());
        assertTrue(stats.get(1).getHits() >= stats.get(2).getHits());
    }

    @Test
    void getStats_shouldReturnUniqueHitsWhenUniqueTrue() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        List<ViewStats> stats = statsRepository.getStats(start, end, null, true);

        assertEquals(3, stats.size());

        ViewStats event1Stats = stats.stream()
                .filter(s -> s.getUri().equals("/events/1") && s.getApp().equals("ewm-main-service"))
                .findFirst()
                .orElse(null);

        assertNotNull(event1Stats);
        assertEquals(2L, event1Stats.getHits());
    }

    @Test
    void getStats_shouldFilterByUris() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        List<String> uris = List.of("/events/1");

        List<ViewStats> stats = statsRepository.getStats(start, end, uris, false);

        assertEquals(2, stats.size());

        stats.forEach(viewStats ->
                assertEquals("/events/1", viewStats.getUri())
        );
    }

    @Test
    void getStats_shouldReturnEmptyListWhenNoMatches() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 2, 0, 0); // После всех хитов
        LocalDateTime end = LocalDateTime.of(2024, 1, 3, 0, 0);

        List<ViewStats> stats = statsRepository.getStats(start, end, null, false);

        assertTrue(stats.isEmpty());
    }

    @Test
    void getStats_shouldHandleTimeRangeCorrectly() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 30); // Между hit1 и hit2
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 30);   // Между hit2 и hit3

        List<ViewStats> stats = statsRepository.getStats(start, end, null, false);

        // Должен быть только hit2 (в диапазоне времени)
        assertEquals(1, stats.size());
        ViewStats stat = stats.get(0);
        assertEquals("/events/1", stat.getUri());
        assertEquals(1L, stat.getHits()); // Один хит в этом диапазоне
    }

    @Test
    void getStats_shouldCountNonUniqueHitsCorrectly() {
        Hit additionalHit = Hit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1") // Тот же IP
                .timestamp(LocalDateTime.of(2024, 1, 1, 14, 0))
                .build();
        entityManager.persist(additionalHit);
        entityManager.flush();

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        List<ViewStats> nonUniqueStats = statsRepository.getStats(start, end, List.of("/events/1"), false);

        ViewStats nonUniqueStat = nonUniqueStats.stream()
                .filter(s -> s.getApp().equals("ewm-main-service"))
                .findFirst()
                .orElse(null);

        assertNotNull(nonUniqueStat);
        assertEquals(3L, nonUniqueStat.getHits()); // Все хиты, включая дубликаты IP


        List<ViewStats> uniqueStats = statsRepository.getStats(start, end, List.of("/events/1"), true);

        ViewStats uniqueStat = uniqueStats.stream()
                .filter(s -> s.getApp().equals("ewm-main-service"))
                .findFirst()
                .orElse(null);

        assertNotNull(uniqueStat);
        assertEquals(2L, uniqueStat.getHits()); // Только уникальные IP
    }
}