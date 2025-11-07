package ru.practicum.main.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.participation.ParticipationRequestDto;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.enums.ParticipationRequestStatus;
import ru.practicum.main.exception.conflict.*;
import ru.practicum.main.exception.notFound.EventNotFoundException;
import ru.practicum.main.exception.notFound.RequestNotFoundException;
import ru.practicum.main.mapper.ParticipationRequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.service.interfaces.RequestService;
import ru.practicum.main.service.interfaces.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса на участие для пользователя: {} для события: {}", userId, eventId);

        userService.checkUserExists(userId);
        Event event = eventService.getEventById(eventId);

        // Проверка, что пользователь не является инициатором события
        if (event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь является инициатором события");
            throw new UserOwnEventException();
        }

        // Проверка, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            log.error("Событие не опубликовано");
            throw new EventNotPublishedException();
        }

        // Проверка на дублирование заявки
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            log.error("Дублирование заявки");
            throw new DuplicateRequestException();
        }

        // Проверка лимита участников
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new EventParticipantLimitException();
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(userService.getUserById(userId))
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        // Если пре-модерация отключена, автоматически подтверждаем заявку
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);

        log.info("Заявка на участие создана id: {}", savedRequest.getId());
        return requestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявки на участие для пользователя: {}", userId);

        userService.checkUserExists(userId);

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки: {} пользователем: {}", requestId, userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        // Проверка, что заявка принадлежит пользователю
        if (!request.getRequester().getId().equals(userId)) {
            log.error("Заявка не принадлежит пользователю");
            throw new RequestNotFoundException(requestId);
        }

        request.setStatus(ParticipationRequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        log.info("Заявка: {} отменена пользователем: {}", requestId, userId);
        return requestMapper.toParticipationRequestDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Получение участников для события: {} пользователем: {}", eventId, userId);

        // Проверка, что пользователь является инициатором события
        Event event = eventService.getEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь не является инициатором события");
            throw new EventNotFoundException(eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статуса запроса на событие: {} пользователем: {}", eventId, userId);

        Event event = eventService.getEventById(eventId);

        // Проверка, что пользователь является инициатором события
        if (!event.getInitiator().getId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findByEventIdAndIdIn(
                eventId, updateRequest.getRequestIds());

        // Проверка, что все заявки находятся в состоянии ожидания
        for (ParticipationRequest request : requests) {
            log.error("Заявки не находятся в состоянии ожидания");
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new RequestStatusException("Запрос должен иметь статус ОЖИДАЮЩИЙ");
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (updateRequest.getStatus().equals("CONFIRMED")) {
            return confirmRequests(event, requests, result);
        } else if (updateRequest.getStatus().equals("REJECTED")) {
            return rejectRequests(requests, result);
        }

        return result;
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event, List<ParticipationRequest> requests,
                                                           EventRequestStatusUpdateResult result) {
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(event.getId());
        Long availableSlots = event.getParticipantLimit() - confirmedRequests;

        if (event.getParticipantLimit() > 0 && availableSlots <= 0) {
            log.error("Превышен лимит участников");
            throw new EventParticipantLimitException();
        }

        int toConfirm = Math.min(requests.size(), availableSlots.intValue());

        for (int i = 0; i < requests.size(); i++) {
            ParticipationRequest request = requests.get(i);

            if (i < toConfirm) {
                request.setStatus(ParticipationRequestStatus.CONFIRMED);
                requestRepository.save(request);
                result.getConfirmedRequests().add(requestMapper.toParticipationRequestDto(request));
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
                requestRepository.save(request);
                result.getRejectedRequests().add(requestMapper.toParticipationRequestDto(request));
            }
        }

        return result;
    }

    private EventRequestStatusUpdateResult rejectRequests(List<ParticipationRequest> requests,
                                                          EventRequestStatusUpdateResult result) {
        log.info("Отмена заявок");
        for (ParticipationRequest request : requests) {
            request.setStatus(ParticipationRequestStatus.REJECTED);
            requestRepository.save(request);
            result.getRejectedRequests().add(requestMapper.toParticipationRequestDto(request));
        }

        return result;
    }
}