package ru.practicum.main.controller.privats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.participation.ParticipationRequestDto;
import ru.practicum.main.service.interfaces.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateRequestControllerUnitTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private PrivateRequestController privateRequestController;

    private final Long userId = 1L;
    private final Long eventId = 1L;
    private final Long requestId = 1L;

    @Test
    void getRequests_ShouldReturnRequestList() {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(userId)
                .event(eventId)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getUserRequests(anyLong())).thenReturn(List.of(requestDto));

        List<ParticipationRequestDto> result = privateRequestController.getRequests(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(requestService, times(1)).getUserRequests(userId);
    }

    @Test
    void createRequest_ShouldReturnCreatedRequest() {
        ParticipationRequestDto expectedResponse = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(userId)
                .event(eventId)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();

        when(requestService.createRequest(anyLong(), anyLong())).thenReturn(expectedResponse);

        ParticipationRequestDto actualResponse = privateRequestController.createRequest(userId, eventId);

        assertNotNull(actualResponse);
        assertEquals(requestId, actualResponse.getId());
        assertEquals("PENDING", actualResponse.getStatus());
        verify(requestService, times(1)).createRequest(userId, eventId);
    }

    @Test
    void cancelRequest_ShouldReturnCanceledRequest() {
        ParticipationRequestDto expectedResponse = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(userId)
                .event(eventId)
                .status("CANCELED")
                .created(LocalDateTime.now())
                .build();

        when(requestService.cancelRequest(anyLong(), anyLong())).thenReturn(expectedResponse);

        ParticipationRequestDto actualResponse = privateRequestController.cancelRequest(userId, requestId);

        assertNotNull(actualResponse);
        assertEquals(requestId, actualResponse.getId());
        assertEquals("CANCELED", actualResponse.getStatus());
        verify(requestService, times(1)).cancelRequest(userId, requestId);
    }

    @Test
    void getEventParticipants_ShouldReturnRequestList() {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(2L)
                .event(eventId)
                .status("CONFIRMED")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getEventParticipants(anyLong(), anyLong())).thenReturn(List.of(requestDto));

        List<ParticipationRequestDto> result = privateRequestController.getEventParticipants(userId, eventId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CONFIRMED", result.get(0).getStatus());
        verify(requestService, times(1)).getEventParticipants(userId, eventId);
    }

    @Test
    void updateRequestStatus_ShouldReturnUpdateResult() {
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(requestId))
                .status("CONFIRMED")
                .build();

        ParticipationRequestDto confirmedRequest = ParticipationRequestDto.builder()
                .id(requestId)
                .status("CONFIRMED")
                .build();

        EventRequestStatusUpdateResult expectedResponse = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(confirmedRequest))
                .rejectedRequests(List.of())
                .build();

        when(requestService.updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(expectedResponse);

        EventRequestStatusUpdateResult actualResponse =
                privateRequestController.updateRequestStatus(userId, eventId, updateRequest);

        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.getConfirmedRequests().size());
        assertEquals(0, actualResponse.getRejectedRequests().size());
        verify(requestService, times(1)).updateRequestStatus(userId, eventId, updateRequest);
    }
}