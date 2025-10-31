package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.StatsRequest;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.service.StatService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping
public class StatsController {
    private final StatService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit createHit(@Valid @RequestBody EndpointHit endpointHit) {
        log.info("Сервер: запрос на сохранение эндпоинта - app: {}, uri: {}, ip: {}",
                endpointHit.getApp(), endpointHit.getUri(), endpointHit.getIp());
        return service.saveHit(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@Valid @ModelAttribute StatsRequest statsRequest) {

        log.info("Сервер: вывод статистики от {} до {}, путь: {}, уникальность ip: {}",
                statsRequest.getStart(), statsRequest.getEnd(), statsRequest.getUris(), statsRequest.getUnique());

        return service.getStats(statsRequest.getStart(), statsRequest.getEnd(), statsRequest.getUris(),
                statsRequest.getUnique());
    }
}