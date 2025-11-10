package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.model.Location;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LocationMapperTest {

    @Autowired
    private LocationMapper locationMapper;

    @Test
    void toEntity_ShouldMapLocationDtoToLocation() {
        // Given
        LocationDto locationDto = LocationDto.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        // When
        Location location = locationMapper.toEntity(locationDto);

        // Then
        assertNotNull(location);
        assertEquals(locationDto.getLat(), location.getLat());
        assertEquals(locationDto.getLon(), location.getLon());
    }

    @Test
    void toDto_ShouldMapLocationToLocationDto() {
        // Given
        Location location = Location.builder()
                .lat(59.9343f)
                .lon(30.3351f)
                .build();

        // When
        LocationDto locationDto = locationMapper.toDto(location);

        // Then
        assertNotNull(locationDto);
        assertEquals(location.getLat(), locationDto.getLat());
        assertEquals(location.getLon(), locationDto.getLon());
    }

    @Test
    void toEntity_ShouldHandleNull() {
        // When
        Location location = locationMapper.toEntity(null);

        // Then
        assertNull(location);
    }

    @Test
    void toDto_ShouldHandleNull() {
        // When
        LocationDto locationDto = locationMapper.toDto(null);

        // Then
        assertNull(locationDto);
    }

    @Test
    void toEntity_ShouldMapWithPrecision() {
        // Given
        LocationDto locationDto = LocationDto.builder()
                .lat(40.7128f)
                .lon(-74.0060f)
                .build();

        // When
        Location location = locationMapper.toEntity(locationDto);

        // Then
        assertEquals(40.7128f, location.getLat(), 0.0001f);
        assertEquals(-74.0060f, location.getLon(), 0.0001f);
    }

    @Test
    void toDto_ShouldMapWithPrecision() {
        // Given
        Location location = Location.builder()
                .lat(51.5074f)
                .lon(-0.1278f)
                .build();

        // When
        LocationDto locationDto = locationMapper.toDto(location);

        // Then
        assertEquals(51.5074f, locationDto.getLat(), 0.0001f);
        assertEquals(-0.1278f, locationDto.getLon(), 0.0001f);
    }
}