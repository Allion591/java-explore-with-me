package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.participation.ParticipationRequestDto;
import ru.practicum.main.enums.ParticipationRequestStatus;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParticipationRequestMapperTest {

    @Autowired
    private ParticipationRequestMapper participationRequestMapper;

    @Test
    void toParticipationRequestDto_ShouldMapParticipationRequestToDto() {
        // Given
        User requester = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        Event event = Event.builder()
                .id(2L)
                .title("Summer Concert")
                .build();

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .id(3L)
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();

        // When
        ParticipationRequestDto dto = participationRequestMapper.toParticipationRequestDto(participationRequest);

        // Then
        assertNotNull(dto);
        assertEquals(participationRequest.getId(), dto.getId());
        assertEquals(participationRequest.getCreated(), dto.getCreated());
        assertEquals(participationRequest.getStatus().name(), dto.getStatus());
        assertEquals(participationRequest.getEvent().getId(), dto.getEvent());
        assertEquals(participationRequest.getRequester().getId(), dto.getRequester());
    }

    @Test
    void toParticipationRequestDto_ShouldMapEventAndRequesterIds() {
        // Given
        User requester = User.builder().id(10L).build();
        Event event = Event.builder().id(20L).build();

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        // When
        ParticipationRequestDto dto = participationRequestMapper.toParticipationRequestDto(participationRequest);

        // Then - проверяем, что маппинг ID событий и пользователей корректен
        assertEquals(20L, dto.getEvent());
        assertEquals(10L, dto.getRequester());
    }

    @Test
    void toParticipationRequestDto_ShouldHandleDifferentStatuses() {
        // Given
        User requester = User.builder().id(1L).build();
        Event event = Event.builder().id(2L).build();

        ParticipationRequest pendingRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest confirmedRequest = ParticipationRequest.builder()
                .id(2L)
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest rejectedRequest = ParticipationRequest.builder()
                .id(3L)
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.REJECTED)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest canceledRequest = ParticipationRequest.builder()
                .id(4L)
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.CANCELED)
                .created(LocalDateTime.now())
                .build();

        // When
        ParticipationRequestDto pendingDto = participationRequestMapper.toParticipationRequestDto(pendingRequest);
        ParticipationRequestDto confirmedDto = participationRequestMapper.toParticipationRequestDto(confirmedRequest);
        ParticipationRequestDto rejectedDto = participationRequestMapper.toParticipationRequestDto(rejectedRequest);
        ParticipationRequestDto canceledDto = participationRequestMapper.toParticipationRequestDto(canceledRequest);

        // Then
        assertEquals("PENDING", pendingDto.getStatus());
        assertEquals("CONFIRMED", confirmedDto.getStatus());
        assertEquals("REJECTED", rejectedDto.getStatus());
        assertEquals("CANCELED", canceledDto.getStatus());
    }

    @Test
    void toParticipationRequestDto_ShouldHandleNull() {
        // When
        ParticipationRequestDto dto = participationRequestMapper.toParticipationRequestDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    void toParticipationRequestDto_ShouldMapCreatedDate() {
        // Given
        LocalDateTime createdDate = LocalDateTime.of(2024, 7, 15, 14, 30, 0);
        User requester = User.builder().id(1L).build();
        Event event = Event.builder().id(2L).build();

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .id(1L)
                .created(createdDate)
                .event(event)
                .requester(requester)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        // When
        ParticipationRequestDto dto = participationRequestMapper.toParticipationRequestDto(participationRequest);

        // Then
        assertEquals(createdDate, dto.getCreated());
    }
}