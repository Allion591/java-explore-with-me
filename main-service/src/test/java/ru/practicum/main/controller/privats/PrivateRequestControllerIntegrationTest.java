package ru.practicum.main.controller.privats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.participation.ParticipationRequestDto;
import ru.practicum.main.service.interfaces.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PrivateRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    private final Long userId = 1L;
    private final Long eventId = 1L;
    private final Long requestId = 1L;

    @Test
    void getRequests_ShouldReturn200AndRequestList() throws Exception {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(userId)
                .event(eventId)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getUserRequests(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(requestService, times(1)).getUserRequests(userId);
    }

    @Test
    void getRequests_WithInvalidUserId_ShouldReturn400() throws Exception {
        Long invalidUserId = 0L;

        mockMvc.perform(get("/users/{userId}/requests", invalidUserId))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).getUserRequests(anyLong());
    }

    @Test
    void createRequest_ShouldReturn201AndParticipationRequest() throws Exception {
        ParticipationRequestDto response = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(userId)
                .event(eventId)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();

        when(requestService.createRequest(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(requestService, times(1)).createRequest(userId, eventId);
    }

    @Test
    void createRequest_WithInvalidIds_ShouldReturn400() throws Exception {
        Long invalidUserId = 0L;
        Long invalidEventId = 0L;

        mockMvc.perform(post("/users/{userId}/requests", invalidUserId)
                        .param("eventId", invalidEventId.toString()))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).createRequest(anyLong(), anyLong());
    }

    @Test
    void cancelRequest_ShouldReturn200AndCanceledRequest() throws Exception {
        ParticipationRequestDto response = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(userId)
                .event(eventId)
                .status("CANCELED")
                .created(LocalDateTime.now())
                .build();

        when(requestService.cancelRequest(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(requestService, times(1)).cancelRequest(userId, requestId);
    }

    @Test
    void cancelRequest_WithInvalidIds_ShouldReturn400() throws Exception {
        Long invalidUserId = 0L;
        Long invalidRequestId = 0L;

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", invalidUserId, invalidRequestId))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).cancelRequest(anyLong(), anyLong());
    }

    @Test
    void getEventParticipants_ShouldReturn200AndRequestList() throws Exception {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(2L)
                .event(eventId)
                .status("CONFIRMED")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getEventParticipants(anyLong(), anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        verify(requestService, times(1)).getEventParticipants(userId, eventId);
    }

    @Test
    void getEventParticipants_WithInvalidIds_ShouldReturn400() throws Exception {
        Long invalidUserId = 0L;
        Long invalidEventId = 0L;

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", invalidUserId, invalidEventId))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).getEventParticipants(anyLong(), anyLong());
    }

    @Test
    void updateRequestStatus_ShouldReturn200AndUpdateResult() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(requestId))
                .status("CONFIRMED")
                .build();

        ParticipationRequestDto confirmedRequest = ParticipationRequestDto.builder()
                .id(requestId)
                .status("CONFIRMED")
                .build();

        EventRequestStatusUpdateResult response = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(confirmedRequest))
                .rejectedRequests(List.of())
                .build();

        when(requestService.updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.length()").value(1))
                .andExpect(jsonPath("$.rejectedRequests.length()").value(0));

        verify(requestService, times(1)).updateRequestStatus(userId, eventId, updateRequest);
    }

    @Test
    void updateRequestStatus_WithInvalidData_ShouldReturn400() throws Exception {
        EventRequestStatusUpdateRequest invalidRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of()) // Пустой список
                .status(null) // Null статус
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verify(requestService, never()).updateRequestStatus(anyLong(), anyLong(), any());
    }

    @Test
    void updateRequestStatus_WithInvalidIds_ShouldReturn400() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(requestId))
                .status("CONFIRMED")
                .build();

        Long invalidUserId = 0L;
        Long invalidEventId = 0L;

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", invalidUserId, invalidEventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).updateRequestStatus(anyLong(), anyLong(), any());
    }
}