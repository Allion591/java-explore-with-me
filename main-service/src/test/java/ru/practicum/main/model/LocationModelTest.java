package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LocationModelTest {

    @Test
    void location_ShouldCreateWithBuilder() {
        // Given & When
        Location location = Location.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        // Then
        assertNotNull(location);
        assertEquals(55.7558f, location.getLat());
        assertEquals(37.6173f, location.getLon());
    }

    @Test
    void location_ShouldHaveNoArgsConstructor() {
        // Given & When
        Location location = new Location();
        location.setLat(59.9343f);
        location.setLon(30.3351f);

        // Then
        assertNotNull(location);
        assertEquals(59.9343f, location.getLat());
        assertEquals(30.3351f, location.getLon());
    }

    @Test
    void location_ShouldHaveAllArgsConstructor() {
        // Given & When
        Location location = new Location(40.7128f, -74.0060f);

        // Then
        assertNotNull(location);
        assertEquals(40.7128f, location.getLat());
        assertEquals(-74.0060f, location.getLon());
    }

    @Test
    void location_ShouldHaveLombokFunctionality() {
        // Given & When
        Location location1 = Location.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        Location location2 = Location.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        Location location3 = Location.builder()
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
    }

    @Test
    void location_ShouldHandleNegativeCoordinates() {
        // Given & When
        Location location = Location.builder()
                .lat(-33.8688f)
                .lon(151.2093f)
                .build();

        // Then
        assertEquals(-33.8688f, location.getLat());
        assertEquals(151.2093f, location.getLon());
    }

    @Test
    void location_ShouldHandleZeroCoordinates() {
        // Given & When
        Location location = Location.builder()
                .lat(0.0f)
                .lon(0.0f)
                .build();

        // Then
        assertEquals(0.0f, location.getLat());
        assertEquals(0.0f, location.getLon());
    }

    @Test
    void location_ShouldUpdateCoordinates() {
        // Given
        Location location = Location.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        // When
        location.setLat(51.5074f);
        location.setLon(-0.1278f);

        // Then
        assertEquals(51.5074f, location.getLat());
        assertEquals(-0.1278f, location.getLon());
    }

    @Test
    void location_ShouldHandlePrecision() {
        // Given & When
        Location location = Location.builder()
                .lat(55.755813f)
                .lon(37.617700f)
                .build();

        // Then
        assertEquals(55.755813f, location.getLat(), 0.000001f);
        assertEquals(37.617700f, location.getLon(), 0.000001f);
    }
}