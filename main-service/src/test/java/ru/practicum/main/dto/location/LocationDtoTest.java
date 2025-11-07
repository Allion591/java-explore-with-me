package ru.practicum.main.dto.location;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LocationDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void locationDto_ShouldSerializeAndDeserialize() throws JsonProcessingException {
        // Given
        LocationDto locationDto = LocationDto.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        // When
        String json = objectMapper.writeValueAsString(locationDto);
        LocationDto deserialized = objectMapper.readValue(json, LocationDto.class);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"lat\":55.7558"));
        assertTrue(json.contains("\"lon\":37.6173"));
        assertEquals(locationDto.getLat(), deserialized.getLat());
        assertEquals(locationDto.getLon(), deserialized.getLon());
    }

    @Test
    void locationDto_ShouldHandlePrecision() throws JsonProcessingException {
        // Given
        String json = "{\"lat\":55.7558,\"lon\":37.6173}";

        // When
        LocationDto locationDto = objectMapper.readValue(json, LocationDto.class);

        // Then
        assertEquals(55.7558f, locationDto.getLat(), 0.0001f);
        assertEquals(37.6173f, locationDto.getLon(), 0.0001f);
    }

    @Test
    void locationDto_ShouldHaveLombokFunctionality() {
        // Given & When
        LocationDto location1 = LocationDto.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        LocationDto location2 = LocationDto.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        LocationDto location3 = LocationDto.builder()
                .lat(59.9343f)
                .lon(30.3351f)
                .build();

        // Then
        assertEquals(location1, location2);
        assertNotEquals(location1, location3);
        assertEquals(location1.hashCode(), location2.hashCode());
        assertNotEquals(location1.hashCode(), location3.hashCode());

        assertNotNull(location1.toString());
        assertTrue(location1.toString().contains("55.7558"));
        assertTrue(location1.toString().contains("37.6173"));

        // Проверка геттеров и сеттеров
        location1.setLat(40.7128f);
        location1.setLon(-74.0060f);
        assertEquals(40.7128f, location1.getLat());
        assertEquals(-74.0060f, location1.getLon());
    }
}