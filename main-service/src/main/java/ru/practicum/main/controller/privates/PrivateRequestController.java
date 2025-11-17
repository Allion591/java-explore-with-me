package ru.practicum.main.controller.privates;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.participation.ParticipationRequestDto;
import ru.practicum.main.service.interfaces.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable @Min(1) Long userId) {
        log.info("Private: получение запросов пользователя с id={}", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Min(1) Long userId,
                                                 @RequestParam @Min(1) Long eventId) {
        log.info("Private: создание запроса на участие пользователем с id={} в событии с id={}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Min(1) Long userId,
                                                 @PathVariable @Min(1) Long requestId) {
        log.info("Private: отмена запроса с id={} пользователем с id={}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable @Min(1) Long userId,
                                                              @PathVariable @Min(1) Long eventId) {
        log.info("Private: получение запросов на участие в событии с id={} пользователя с id={}", eventId, userId);
        return requestService.getEventParticipants(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable @Min(1) Long userId,
                                                              @PathVariable @Min(1) Long eventId,
                                                              @Valid @RequestBody
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Private: обновление статуса запросов на участие в событии с id={} пользователем с id={}",
                eventId, userId);
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}