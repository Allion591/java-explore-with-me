package ru.practicum.main.stat;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.StatsRequest;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.statsClient.StatsClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionToStatisticsImpl implements ConnectionToStatistics {
    private final StatsClient statsClient;

    @Override
    public void postHit(HttpServletRequest request) {
        EndpointHit endpointHit = EndpointHit.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.postHit(endpointHit);
    }

    @Override
    public Map<String, Long> getViews(List<String> uris) {
        try {
            StatsRequest statsRequest = StatsRequest.builder()
                    .start(LocalDateTime.now().minusYears(100))
                    .end(LocalDateTime.now())
                    .uris(uris)
                    .unique(true)
                    .build();

            List<ViewStats> stats = statsClient.getStats(statsRequest);

            return stats.stream()
                    .collect(Collectors.toMap(
                            ViewStats::getUri,
                            ViewStats::getHits
                    ));
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return Map.of();
        }
    }
}