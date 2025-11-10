package ru.practicum.main.dto.filter;

import org.junit.jupiter.api.Test;
import ru.practicum.main.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminEventFilterParamsTest {

    @Test
    void adminEventFilterParams_ShouldCreateWithDefaultValues() {
        // When
        AdminEventFilterParams params = AdminEventFilterParams.builder().build();

        // Then
        assertNotNull(params);
        assertEquals(0, params.getFrom());
        assertEquals(10, params.getSize());
        assertNull(params.getUsers());
        assertNull(params.getStates());
        assertNull(params.getCategories());
        assertNull(params.getRangeStart());
        assertNull(params.getRangeEnd());
    }

    @Test
    void adminEventFilterParams_ShouldCreateWithCustomValues() {
        // Given
        List<Long> users = List.of(1L, 2L);
        List<Long> categories = List.of(3L, 4L);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        AdminEventFilterParams params = AdminEventFilterParams.builder()
                .users(users)
                .categories(categories)
                .rangeStart(start)
                .rangeEnd(end)
                .from(5)
                .size(20)
                .build();

        // Then
        assertNotNull(params);
        assertEquals(users, params.getUsers());
        assertEquals(categories, params.getCategories());
        assertEquals(start, params.getRangeStart());
        assertEquals(end, params.getRangeEnd());
        assertEquals(5, params.getFrom());
        assertEquals(20, params.getSize());
    }

    @Test
    void adminEventFilterParams_JsonCreatorShouldParseStates() {
        // When
        AdminEventFilterParams params = AdminEventFilterParams.create(
                List.of(1L, 2L),
                List.of("PENDING", "PUBLISHED", "INVALID_STATE"),
                List.of(3L, 4L),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                0,
                10
        );

        // Then
        assertNotNull(params);
        assertEquals(List.of(1L, 2L), params.getUsers());
        assertEquals(List.of(EventState.PENDING, EventState.PUBLISHED), params.getStates()); // INVALID_STATE должен быть отфильтрован
        assertEquals(List.of(3L, 4L), params.getCategories());
        assertNotNull(params.getRangeStart());
        assertNotNull(params.getRangeEnd());
        assertEquals(0, params.getFrom());
        assertEquals(10, params.getSize());
    }
}