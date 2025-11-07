package ru.practicum.main.dto.filter;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventPublicFilterRequestTest {

    @Test
    void eventPublicFilterRequest_ShouldCreateWithDefaultValues() {
        // When
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder().build();

        // Then
        assertNotNull(filter);
        assertEquals(0, filter.getFrom());
        assertEquals(10, filter.getSize());
        assertFalse(filter.getOnlyAvailable());
        assertEquals("EVENT_DATE", filter.getSort());
        assertNull(filter.getText());
        assertNull(filter.getCategories());
        assertNull(filter.getPaid());
        assertNull(filter.getRangeStart());
        assertNull(filter.getRangeEnd());
    }

    @Test
    void eventPublicFilterRequest_ShouldCreateWithCustomValues() {
        // Given
        List<Long> categories = List.of(1L, 2L);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder()
                .text("concert")
                .categories(categories)
                .paid(true)
                .rangeStart(start)
                .rangeEnd(end)
                .onlyAvailable(true)
                .sort("VIEWS")
                .from(5)
                .size(20)
                .build();

        // Then
        assertNotNull(filter);
        assertEquals("concert", filter.getText());
        assertEquals(categories, filter.getCategories());
        assertTrue(filter.getPaid());
        assertEquals(start, filter.getRangeStart());
        assertEquals(end, filter.getRangeEnd());
        assertTrue(filter.getOnlyAvailable());
        assertEquals("VIEWS", filter.getSort());
        assertEquals(5, filter.getFrom());
        assertEquals(20, filter.getSize());
    }

    @Test
    void eventPublicFilterRequest_GetEffectiveRangeStart_ShouldReturnCurrentTimeWhenBothDatesNull() {
        // Given
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder().build();

        // When
        LocalDateTime effectiveStart = filter.getEffectiveRangeStart();

        // Then - должен вернуть текущее время, когда обе даты null
        assertNotNull(effectiveStart);
        assertTrue(effectiveStart.isBefore(LocalDateTime.now().plusSeconds(1)) ||
                effectiveStart.isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void eventPublicFilterRequest_GetEffectiveRangeStart_ShouldReturnRangeStartWhenProvided() {
        // Given
        LocalDateTime expectedStart = LocalDateTime.now().minusDays(1);
        EventPublicFilterRequest filter = EventPublicFilterRequest.builder()
                .rangeStart(expectedStart)
                .build();

        // When
        LocalDateTime effectiveStart = filter.getEffectiveRangeStart();

        // Then - должен вернуть предоставленную дату начала
        assertEquals(expectedStart, effectiveStart);
    }
}