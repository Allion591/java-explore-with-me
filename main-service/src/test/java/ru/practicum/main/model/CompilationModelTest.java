package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompilationModelTest {

    @Test
    void compilation_ShouldCreateWithBuilder() {
        // Given & When
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .events(new HashSet<>())
                .build();

        // Then
        assertNotNull(compilation);
        assertEquals(1L, compilation.getId());
        assertEquals("Summer Events", compilation.getTitle());
        assertTrue(compilation.getPinned());
        assertNotNull(compilation.getEvents());
        assertTrue(compilation.getEvents().isEmpty());
    }

    @Test
    void compilation_ShouldUseDefaultValues() {
        // Given & When - создание без указания pinned и events
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .build();

        // Then - должны использоваться значения по умолчанию
        assertNotNull(compilation);
        assertEquals(1L, compilation.getId());
        assertEquals("Test Compilation", compilation.getTitle());
        assertFalse(compilation.getPinned()); // pinned = false по умолчанию
        assertNotNull(compilation.getEvents()); // events должен быть пустым HashSet
        assertTrue(compilation.getEvents().isEmpty());
    }

    @Test
    void compilation_ShouldHaveNoArgsConstructor() {
        // Given & When
        Compilation compilation = new Compilation();
        compilation.setId(1L);
        compilation.setTitle("Winter Events");
        compilation.setPinned(true);
        compilation.setEvents(new HashSet<>());

        // Then
        assertNotNull(compilation);
        assertEquals(1L, compilation.getId());
        assertEquals("Winter Events", compilation.getTitle());
        assertTrue(compilation.getPinned());
        assertNotNull(compilation.getEvents());
    }

    @Test
    void compilation_ShouldHaveAllArgsConstructor() {
        // Given & When
        Set<Event> events = new HashSet<>();
        Compilation compilation = new Compilation(1L, "Music Events", true, events);

        // Then
        assertNotNull(compilation);
        assertEquals(1L, compilation.getId());
        assertEquals("Music Events", compilation.getTitle());
        assertTrue(compilation.getPinned());
        assertEquals(events, compilation.getEvents());
    }

    @Test
    void compilation_ShouldHaveLombokFunctionality() {
        // Given & When
        Compilation compilation1 = Compilation.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .build();

        Compilation compilation2 = Compilation.builder()
                .id(1L)
                .title("Summer Events")
                .pinned(true)
                .build();

        Compilation compilation3 = Compilation.builder()
                .id(2L)
                .title("Winter Events")
                .pinned(false)
                .build();

        // Then
        assertEquals(compilation1, compilation2);
        assertNotEquals(compilation1, compilation3);
        assertEquals(compilation1.hashCode(), compilation2.hashCode());
        assertNotEquals(compilation1.hashCode(), compilation3.hashCode());

        assertNotNull(compilation1.toString());
        assertTrue(compilation1.toString().contains("Summer Events"));
    }

    @Test
    void compilation_ShouldHandleEventsSet() {
        // Given
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .build();

        Event event1 = Event.builder().id(1L).title("Event 1").build();
        Event event2 = Event.builder().id(2L).title("Event 2").build();

        Set<Event> events = new HashSet<>();
        events.add(event1);
        events.add(event2);

        // When
        compilation.setEvents(events);

        // Then
        assertNotNull(compilation.getEvents());
        assertEquals(2, compilation.getEvents().size());
        assertTrue(compilation.getEvents().contains(event1));
        assertTrue(compilation.getEvents().contains(event2));
    }

    @Test
    void compilation_ShouldUpdatePinnedField() {
        // Given
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(false)
                .build();

        // When
        compilation.setPinned(true);

        // Then
        assertTrue(compilation.getPinned());
    }
}