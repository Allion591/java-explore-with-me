package ru.practicum.main.stat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ConnectionToStatistics {

    void postHit(HttpServletRequest request);

    Map<String, Long> getViews(List<String> uris);
}