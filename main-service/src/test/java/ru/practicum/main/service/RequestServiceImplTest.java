package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.main.model.User;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.service.implementations.RequestServiceImpl;
import ru.practicum.main.service.interfaces.EventService;
import ru.practicum.main.service.interfaces.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private ParticipationRequestMapper requestMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final Long userId = 1L;
    private final Long eventId = 1L;
    private final Long requestId = 1L;
    private final User initiator = User.builder().id(1L).name("Initiator").build();
    private final User participant = User.builder().id(2L).name("Participant").build();
    private final Event event = Event.builder()
            .id(eventId)
            .initiator(initiator)
            .state(EventState.PUBLISHED)
            .participantLimit(10)
            .requestModeration(true)
            .build();

    @Test
    void getUserRequests_shouldReturnUserRequests() {
        // Arrange
        ParticipationRequest request = ParticipationRequest.builder().id(requestId).build();
        when(requestRepository.findByRequesterId(userId)).thenReturn(List.of(request));

        ParticipationRequestDto expectedDto = ParticipationRequestDto.builder().id(requestId).build();
        when(requestMapper.toParticipationRequestDto(request)).thenReturn(expectedDto);

        // Act
        List<ParticipationRequestDto> result = requestService.getUserRequests(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(requestRepository).findByRequesterId(userId);
        verify(requestMapper).toParticipationRequestDto(request);
    }

    @Test
    void cancelRequest_shouldCancelRequestSuccessfully() {
        // Arrange
        ParticipationRequest request = ParticipationRequest.builder()
                .id(requestId)
                .requester(participant)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        ParticipationRequest canceledRequest = ParticipationRequest.builder()
                .id(requestId)
                .status(ParticipationRequestStatus.CANCELED)
                .build();
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(canceledRequest);

        ParticipationRequestDto expectedDto = ParticipationRequestDto.builder().id(requestId).build();
        when(requestMapper.toParticipationRequestDto(canceledRequest)).thenReturn(expectedDto);

        // Act
        ParticipationRequestDto result = requestService.cancelRequest(participant.getId(), requestId);

        // Assert
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(requestRepository).save(argThat(req ->
                req.getStatus() == ParticipationRequestStatus.CANCELED
        ));
    }

    @Test
    void cancelRequest_whenRequestNotFound_shouldThrowException() {
        // Arrange
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RequestNotFoundException.class, () ->
                requestService.cancelRequest(userId, requestId)
        );
    }

    @Test
    void cancelRequest_whenRequestNotBelongsToUser_shouldThrowException() {
        // Arrange
        ParticipationRequest request = ParticipationRequest.builder()
                .id(requestId)
                .requester(initiator) // Другой пользователь
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(RequestNotFoundException.class, () ->
                requestService.cancelRequest(participant.getId(), requestId)
        );
    }

    @Test
    void getEventParticipants_shouldReturnParticipants() {
        // Arrange
        when(eventService.getEventById(eventId)).thenReturn(event);

        ParticipationRequest request = ParticipationRequest.builder().id(requestId).build();
        when(requestRepository.findByEventId(eventId)).thenReturn(List.of(request));

        ParticipationRequestDto expectedDto = ParticipationRequestDto.builder().id(requestId).build();
        when(requestMapper.toParticipationRequestDto(request)).thenReturn(expectedDto);

        // Act
        List<ParticipationRequestDto> result = requestService.getEventParticipants(initiator.getId(), eventId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(requestRepository).findByEventId(eventId);
    }

    @Test
    void getEventParticipants_whenUserNotInitiator_shouldThrowException() {
        // Arrange
        when(eventService.getEventById(eventId)).thenReturn(event);

        // Act & Assert
        assertThrows(EventNotFoundException.class, () ->
                requestService.getEventParticipants(participant.getId(), eventId)
        );
    }

    @Test
    void updateRequestStatus_shouldConfirmRequests() {
        // Arrange
        when(eventService.getEventById(eventId)).thenReturn(event);

        List<Long> requestIds = List.of(1L, 2L);
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status("CONFIRMED")
                .build();

        ParticipationRequest request1 = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .build();
        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(2L)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        when(requestRepository.findByEventIdAndIdIn(eventId, requestIds))
                .thenReturn(List.of(request1, request2));
        when(requestRepository.countConfirmedRequestsByEventId(eventId)).thenReturn(0L);
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(requestMapper.toParticipationRequestDto(any(ParticipationRequest.class)))
                .thenAnswer(invocation -> {
                    ParticipationRequest req = invocation.getArgument(0);
                    return ParticipationRequestDto.builder().id(req.getId()).build();
                });

        // Act
        EventRequestStatusUpdateResult result = requestService.updateRequestStatus(
                initiator.getId(), eventId, updateRequest);

        // Assert
        assertEquals(2, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());
        verify(requestRepository, times(2)).save(any(ParticipationRequest.class));
    }

    @Test
    void updateRequestStatus_whenParticipantLimitReached_shouldThrowException() {
        // Arrange
        Event limitedEvent = Event.builder()
                .id(eventId)
                .initiator(initiator)
                .state(EventState.PUBLISHED)
                .participantLimit(1)
                .requestModeration(true)
                .build();

        when(eventService.getEventById(eventId)).thenReturn(limitedEvent);

        List<Long> requestIds = List.of(1L, 2L);
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status("CONFIRMED")
                .build();

        ParticipationRequest request1 = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .build();
        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(2L)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        when(requestRepository.findByEventIdAndIdIn(eventId, requestIds))
                .thenReturn(List.of(request1, request2));
        when(requestRepository.countConfirmedRequestsByEventId(eventId)).thenReturn(1L);

        // Act & Assert
        assertThrows(EventParticipantLimitException.class, () ->
                requestService.updateRequestStatus(initiator.getId(), eventId, updateRequest)
        );
    }

    @Test
    void updateRequestStatus_whenRequestNotPending_shouldThrowException() {
        // Arrange
        when(eventService.getEventById(eventId)).thenReturn(event);

        List<Long> requestIds = List.of(1L);
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status("CONFIRMED")
                .build();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.CONFIRMED) // Уже подтвержден
                .build();

        when(requestRepository.findByEventIdAndIdIn(eventId, requestIds))
                .thenReturn(List.of(request));

        // Act & Assert
        assertThrows(RequestStatusException.class, () ->
                requestService.updateRequestStatus(initiator.getId(), eventId, updateRequest)
        );
    }

    @Test
    void updateRequestStatus_shouldRejectRequests() {
        // Arrange
        when(eventService.getEventById(eventId)).thenReturn(event);

        List<Long> requestIds = List.of(1L);
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status("REJECTED")
                .build();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        when(requestRepository.findByEventIdAndIdIn(eventId, requestIds))
                .thenReturn(List.of(request));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(request);
        when(requestMapper.toParticipationRequestDto(request))
                .thenReturn(ParticipationRequestDto.builder().id(1L).build());

        // Act
        EventRequestStatusUpdateResult result = requestService.updateRequestStatus(
                initiator.getId(), eventId, updateRequest);

        // Assert
        assertEquals(0, result.getConfirmedRequests().size());
        assertEquals(1, result.getRejectedRequests().size());
        verify(requestRepository).save(argThat(req ->
                req.getStatus() == ParticipationRequestStatus.REJECTED
        ));
    }
}