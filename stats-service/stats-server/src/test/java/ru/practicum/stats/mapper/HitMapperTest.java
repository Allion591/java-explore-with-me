package ru.practicum.stats.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HitMapperTest {

    private final HitMapper hitMapper = Mappers.getMapper(HitMapper.class);

    @Test
    void toHit_shouldMapEndpointHitToHit() {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setId(1L);
        endpointHit.setApp("ewm-main-service");
        endpointHit.setUri("/events/1");
        endpointHit.setIp("192.168.1.1");
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 10, 0);
        endpointHit.setTimestamp(timestamp);

        Hit hit = hitMapper.toHit(endpointHit);

        assertNotNull(hit);
        assertEquals(1L, hit.getId());
        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events/1", hit.getUri());
        assertEquals("192.168.1.1", hit.getIp());
        assertEquals(timestamp, hit.getTimestamp());
    }

    @Test
    void toHit_shouldHandleNull() {
        Hit hit = hitMapper.toHit(null);

        assertNull(hit);
    }

    @Test
    void toEndpointHit_shouldMapHitToEndpointHit() {
        Hit hit = Hit.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        EndpointHit endpointHit = hitMapper.toEndpointHit(hit);

        assertNotNull(endpointHit);
        assertEquals(1L, endpointHit.getId());
        assertEquals("ewm-main-service", endpointHit.getApp());
        assertEquals("/events/1", endpointHit.getUri());
        assertEquals("192.168.1.1", endpointHit.getIp());
        assertEquals(hit.getTimestamp(), endpointHit.getTimestamp());
    }

    @Test
    void toEndpointHit_shouldHandleNull() {
        EndpointHit endpointHit = hitMapper.toEndpointHit(null);

        assertNull(endpointHit);
    }

    @Test
    void bidirectionalMapping_shouldBeConsistent() {
        EndpointHit originalEndpointHit = new EndpointHit();
        originalEndpointHit.setId(1L);
        originalEndpointHit.setApp("ewm-main-service");
        originalEndpointHit.setUri("/events/1");
        originalEndpointHit.setIp("192.168.1.1");
        originalEndpointHit.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

        Hit hit = hitMapper.toHit(originalEndpointHit);
        EndpointHit mappedEndpointHit = hitMapper.toEndpointHit(hit);

        assertEquals(originalEndpointHit.getId(), mappedEndpointHit.getId());
        assertEquals(originalEndpointHit.getApp(), mappedEndpointHit.getApp());
        assertEquals(originalEndpointHit.getUri(), mappedEndpointHit.getUri());
        assertEquals(originalEndpointHit.getIp(), mappedEndpointHit.getIp());
        assertEquals(originalEndpointHit.getTimestamp(), mappedEndpointHit.getTimestamp());
    }
}