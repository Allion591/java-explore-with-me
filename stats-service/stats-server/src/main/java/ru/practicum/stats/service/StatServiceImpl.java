package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsPersistenceException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatsRepository statsRepository;
    private final HitMapper mapper;

    @Override
    public EndpointHit saveHit(EndpointHit endpointHit) {
        try {
            log.info("Сервис принял запрос на сохранение статистики - app: {}, uri: {}, ip: {}",
                    endpointHit.getApp(), endpointHit.getUri(), endpointHit.getIp());
            return mapper.toEndpointHit(statsRepository.save(mapper.toHit(endpointHit)));
        } catch (DataAccessException e) {
            log.error("Ошибка при сохранении статистики в БД: {}", e.getMessage());
            throw new StatsPersistenceException(e.getMessage());
        }
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        log.info("Сервис: вывод статистики от {} до {}, путь: {}, уникальность ip: {}", start, end, uris, unique);

        validateTimeRange(start, end);

        return statsRepository.getStats(start, end, uris, Boolean.TRUE.equals(unique));
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Дата начала и окончания не могут быть null");
        }

        if (start.isAfter(end)) {
            throw new ValidationException(
                    String.format("Дата начала %s не может быть позже даты окончания %s", start, end)
            );
        }
    }
}