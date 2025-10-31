package ru.practicum.stats.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsPersistenceException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private HitMapper hitMapper;

    @InjectMocks
    private StatServiceImpl statService;

    private EndpointHit endpointHit;
    private Hit hit;

    @BeforeEach
    void setUp() {
        endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-main-service");
        endpointHit.setUri("/events/1");
        endpointHit.setIp("192.168.1.1");
        endpointHit.setTimestamp(LocalDateTime.now());

        hit = Hit.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void saveHit_shouldSaveAndReturnEndpointHit() {

        when(hitMapper.toHit(endpointHit)).thenReturn(hit);
        when(statsRepository.save(hit)).thenReturn(hit);
        when(hitMapper.toEndpointHit(hit)).thenReturn(endpointHit);

        EndpointHit result = statService.saveHit(endpointHit);

        assertNotNull(result);
        verify(hitMapper).toHit(endpointHit);
        verify(statsRepository).save(hit);
        verify(hitMapper).toEndpointHit(hit);
    }

    @Test
    void saveHit_shouldThrowStatsPersistenceExceptionOnDataAccessError() {
        when(hitMapper.toHit(endpointHit)).thenReturn(hit);
        when(statsRepository.save(hit)).thenThrow(new DataAccessException("Database error") {
        });

        StatsPersistenceException exception = assertThrows(StatsPersistenceException.class,
                () -> statService.saveHit(endpointHit));

        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void getStats_shouldReturnStatsWhenValidParameters() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        List<String> uris = List.of("/events/1", "/events/2");
        Boolean unique = true;

        List<ViewStats> expectedStats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 15L),
                new ViewStats("ewm-main-service", "/events/2", 10L)
        );

        when(statsRepository.getStats(start, end, uris, true)).thenReturn(expectedStats);

        List<ViewStats> result = statService.getStats(start, end, uris, unique);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(statsRepository).getStats(start, end, uris, true);
    }

    @Test
    void getStats_shouldHandleNullUris() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        Boolean unique = false;

        List<ViewStats> expectedStats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 25L)
        );

        when(statsRepository.getStats(start, end, null, false)).thenReturn(expectedStats);

        List<ViewStats> result = statService.getStats(start, end, null, unique);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsRepository).getStats(start, end, null, false);
    }

    @Test
    void getStats_shouldHandleNullUnique() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        List<String> uris = List.of("/events/1");

        List<ViewStats> expectedStats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 15L)
        );

        when(statsRepository.getStats(start, end, uris, false)).thenReturn(expectedStats);

        List<ViewStats> result = statService.getStats(start, end, uris, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(statsRepository).getStats(start, end, uris, false);
    }

    @Test
    void getStats_shouldThrowValidationExceptionWhenStartIsNull() {
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> statService.getStats(null, end, null, null));

        assertEquals("Дата начала и окончания не могут быть null", exception.getMessage());
    }

    @Test
    void getStats_shouldThrowValidationExceptionWhenEndIsNull() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> statService.getStats(start, null, null, null));

        assertEquals("Дата начала и окончания не могут быть null", exception.getMessage());
    }

    @Test
    void getStats_shouldThrowValidationExceptionWhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 2, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 0, 0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> statService.getStats(start, end, null, null));

        assertTrue(exception.getMessage().contains("не может быть позже"));
    }

    @Test
    void getStats_shouldCallRepositoryWithCorrectUniqueFlag() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        when(statsRepository.getStats(start, end, null, true)).thenReturn(List.of());

        statService.getStats(start, end, null, true);

        verify(statsRepository).getStats(start, end, null, true);
    }
}