package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.enums.ParticipationRequestStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParticipationRequestModelTest {

    @Test
    void participationRequest_ShouldCreateWithBuilder() {
        // Given & When
        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        // Then
        assertNotNull(request);
        assertEquals(1L, request.getId());
        assertEquals(ParticipationRequestStatus.CONFIRMED, request.getStatus());
        assertNotNull(request.getCreated());
        assertNull(request.getEvent());
        assertNull(request.getRequester());
    }

    @Test
    void participationRequest_ShouldUseDefaultStatus() {
        // Given & When - создание без указания статуса
        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .build();

        // Then - статус должен быть PENDING по умолчанию
        assertNotNull(request);
        assertEquals(ParticipationRequestStatus.PENDING, request.getStatus());
    }

    @Test
    void participationRequest_ShouldHaveNoArgsConstructor() {
        // Given & When
        ParticipationRequest request = new ParticipationRequest();
        request.setId(1L);
        request.setStatus(ParticipationRequestStatus.REJECTED);

        // Then
        assertNotNull(request);
        assertEquals(1L, request.getId());
        assertEquals(ParticipationRequestStatus.REJECTED, request.getStatus());
    }

    @Test
    void participationRequest_ShouldHaveAllArgsConstructor() {
        // Given & When
        LocalDateTime created = LocalDateTime.now();
        ParticipationRequest request = new ParticipationRequest(1L, null, null,
                ParticipationRequestStatus.CANCELED, created);

        // Then
        assertNotNull(request);
        assertEquals(1L, request.getId());
        assertEquals(ParticipationRequestStatus.CANCELED, request.getStatus());
        assertEquals(created, request.getCreated());
        assertNull(request.getEvent());
        assertNull(request.getRequester());
    }

    @Test
    void participationRequest_ShouldHaveLombokFunctionality() {
        // Given & When
        LocalDateTime created = LocalDateTime.now();
        ParticipationRequest request1 = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .created(created)
                .build();

        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .created(created)
                .build();

        ParticipationRequest request3 = ParticipationRequest.builder()
                .id(2L)
                .status(ParticipationRequestStatus.CONFIRMED)
                .created(created.plusHours(1))
                .build();

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());

        assertNotNull(request1.toString());
        assertTrue(request1.toString().contains("PENDING"));
    }

    @Test
    void participationRequest_ShouldHandleRelationships() {
        // Given
        User user = User.builder().id(1L).name("Requester").build();
        Event event = Event.builder().id(1L).title("Event").build();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        // Then
        assertNotNull(request);
        assertEquals(event, request.getEvent());
        assertEquals(user, request.getRequester());
    }

    @Test
    void participationRequest_ShouldUpdateStatus() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        // When
        request.setStatus(ParticipationRequestStatus.CONFIRMED);

        // Then
        assertEquals(ParticipationRequestStatus.CONFIRMED, request.getStatus());
    }

    @Test
    void participationRequest_ShouldHandleAllStatuses() {
        // Given & When
        ParticipationRequest pending = ParticipationRequest.builder()
                .id(1L)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        ParticipationRequest confirmed = ParticipationRequest.builder()
                .id(2L)
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();

        ParticipationRequest rejected = ParticipationRequest.builder()
                .id(3L)
                .status(ParticipationRequestStatus.REJECTED)
                .build();

        ParticipationRequest canceled = ParticipationRequest.builder()
                .id(4L)
                .status(ParticipationRequestStatus.CANCELED)
                .build();

        // Then
        assertEquals(ParticipationRequestStatus.PENDING, pending.getStatus());
        assertEquals(ParticipationRequestStatus.CONFIRMED, confirmed.getStatus());
        assertEquals(ParticipationRequestStatus.REJECTED, rejected.getStatus());
        assertEquals(ParticipationRequestStatus.CANCELED, canceled.getStatus());
    }

    @Test
    void participationRequest_ShouldHandleCreationTimestamp() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .build();

        // When - создаем дату и устанавливаем
        LocalDateTime createdDate = LocalDateTime.now().minusHours(1);
        request.setCreated(createdDate);

        // Then
        assertEquals(createdDate, request.getCreated());
    }
}