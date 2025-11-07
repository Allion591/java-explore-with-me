package ru.practicum.main.dto.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.dto.user.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventShortDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void eventShortDto_ShouldSerializeToJson() throws JsonProcessingException {
        // Given
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great summer concert event")
                .eventDate(LocalDateTime.of(2024, 7, 15, 19, 0, 0))
                .paid(true)
                .confirmedRequests(50L)
                .views(150L)
                .category(CategoryDto.builder().id(1L).name("Concerts").build())
                .initiator(UserShortDto.builder().id(1L).name("John Doe").build())
                .location(LocationDto.builder().lat(55.7558f).lon(37.6173f).build())
                .build();

        // When
        String json = objectMapper.writeValueAsString(eventShortDto);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"title\":\"Summer Concert\""));
        assertTrue(json.contains("\"annotation\":\"Great summer concert event\""));
        assertTrue(json.contains("\"paid\":true"));
        assertTrue(json.contains("\"confirmedRequests\":50"));
        assertTrue(json.contains("\"views\":150"));
        assertTrue(json.contains("\"eventDate\":\"2024-07-15 19:00:00\""));
        assertTrue(json.contains("\"category\":{\"id\":1,\"name\":\"Concerts\"}"));
        assertTrue(json.contains("\"initiator\":{\"id\":1,\"name\":\"John Doe\"}"));
        assertTrue(json.contains("\"location\":{\"lat\":55.7558,\"lon\":37.6173}"));
    }

    @Test
    void eventShortDto_ShouldDeserializeFromJson() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"annotation\":\"Great summer concert event\"," +
                "\"eventDate\":\"2024-07-15 19:00:00\"," +
                "\"paid\":true," +
                "\"confirmedRequests\":50," +
                "\"views\":150," +
                "\"category\":{\"id\":1,\"name\":\"Concerts\"}," +
                "\"initiator\":{\"id\":1,\"name\":\"John Doe\"}," +
                "\"location\":{\"lat\":55.7558,\"lon\":37.6173}" +
                "}";

        // When
        EventShortDto eventShortDto = objectMapper.readValue(json, EventShortDto.class);

        // Then
        assertNotNull(eventShortDto);
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Summer Concert", eventShortDto.getTitle());
        assertEquals("Great summer concert event", eventShortDto.getAnnotation());
        assertTrue(eventShortDto.getPaid());
        assertEquals(50L, eventShortDto.getConfirmedRequests());
        assertEquals(150L, eventShortDto.getViews());
        assertEquals(LocalDateTime.of(2024, 7, 15, 19, 0, 0),
                eventShortDto.getEventDate());

        assertNotNull(eventShortDto.getCategory());
        assertEquals(1L, eventShortDto.getCategory().getId());
        assertEquals("Concerts", eventShortDto.getCategory().getName());

        assertNotNull(eventShortDto.getInitiator());
        assertEquals(1L, eventShortDto.getInitiator().getId());
        assertEquals("John Doe", eventShortDto.getInitiator().getName());

        assertNotNull(eventShortDto.getLocation());
        assertEquals(55.7558f, eventShortDto.getLocation().getLat(), 0.0001f);
        assertEquals(37.6173f, eventShortDto.getLocation().getLon(), 0.0001f);
    }

    @Test
    void eventShortDto_ShouldHandleNullValues() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"annotation\":null," +
                "\"eventDate\":\"2024-07-15 19:00:00\"," +
                "\"paid\":null," +
                "\"confirmedRequests\":null," +
                "\"views\":null," +
                "\"category\":null," +
                "\"initiator\":null," +
                "\"location\":null" +
                "}";

        // When
        EventShortDto eventShortDto = objectMapper.readValue(json, EventShortDto.class);

        // Then
        assertNotNull(eventShortDto);
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Summer Concert", eventShortDto.getTitle());
        assertNull(eventShortDto.getAnnotation());
        assertNull(eventShortDto.getPaid());
        assertNull(eventShortDto.getConfirmedRequests());
        assertNull(eventShortDto.getViews());
        assertNull(eventShortDto.getCategory());
        assertNull(eventShortDto.getInitiator());
        assertNull(eventShortDto.getLocation());
    }

    @Test
    void eventShortDto_ShouldHandlePartialData() throws JsonProcessingException {
        // Given - минимальный набор полей
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"eventDate\":\"2024-07-15 19:00:00\"" +
                "}";

        // When
        EventShortDto eventShortDto = objectMapper.readValue(json, EventShortDto.class);

        // Then
        assertNotNull(eventShortDto);
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Summer Concert", eventShortDto.getTitle());
        assertEquals(LocalDateTime.of(2024, 7, 15, 19, 0, 0),
                eventShortDto.getEventDate());
        assertNull(eventShortDto.getAnnotation());
        assertNull(eventShortDto.getPaid());
        assertNull(eventShortDto.getConfirmedRequests());
        assertNull(eventShortDto.getViews());
        assertNull(eventShortDto.getCategory());
        assertNull(eventShortDto.getInitiator());
        assertNull(eventShortDto.getLocation());
    }

    @Test
    void eventShortDto_ShouldHaveLombokFunctionality() {
        LocalDateTime eventDate1 = LocalDateTime.now().plusDays(5);
        LocalDateTime eventDate2 = eventDate1;
        LocalDateTime eventDate3 = LocalDateTime.now().plusDays(10);

        CategoryDto concertsCategory = CategoryDto.builder().id(1L).name("Concerts").build();
        CategoryDto festivalsCategory = CategoryDto.builder().id(2L).name("Festivals").build();

        // Given & When
        EventShortDto event1 = EventShortDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great event")
                .eventDate(eventDate1)
                .paid(true)
                .confirmedRequests(50L)
                .views(100L)
                .category(concertsCategory)
                .build();

        EventShortDto event2 = EventShortDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great event")
                .eventDate(eventDate2)
                .paid(true)
                .confirmedRequests(50L)
                .views(100L)
                .category(concertsCategory)
                .build();

        EventShortDto event3 = EventShortDto.builder()
                .id(2L)
                .title("Winter Festival")
                .annotation("Winter event")
                .eventDate(eventDate3)
                .paid(false)
                .confirmedRequests(25L)
                .views(75L)
                .category(festivalsCategory)
                .build();

        assertEquals(event1, event2, "Объекты с одинаковыми полями должны быть равны");
        assertNotEquals(event1, event3, "Объекты с разными полями не должны быть равны");
        assertEquals(event1.hashCode(), event2.hashCode(), "HashCode должен быть " +
                "одинаковым для равных объектов");
        assertNotEquals(event1.hashCode(), event3.hashCode(), "HashCode должен быть " +
                "разным для разных объектов");

        assertNotNull(event1.toString(), "toString не должен возвращать null");
        assertTrue(event1.toString().contains("Summer Concert"), "toString должен содержать название события");

        event1.setTitle("Updated Title");
        assertEquals("Updated Title", event1.getTitle(), "Сеттер title должен работать корректно");

        event1.setPaid(false);
        assertFalse(event1.getPaid(), "Сеттер paid должен работать корректно");

        event1.setConfirmedRequests(75L);
        assertEquals(75L, event1.getConfirmedRequests(), "Сеттер confirmedRequests" +
                " должен работать корректно");

        event1.setViews(200L);
        assertEquals(200L, event1.getViews(), "Сеттер views должен работать корректно");

        CategoryDto newCategory = CategoryDto.builder().id(3L).name("Sports").build();
        event1.setCategory(newCategory);
        assertEquals(newCategory, event1.getCategory(), "Сеттер category должен работать корректно");

        UserShortDto newInitiator = UserShortDto.builder().id(2L).name("Jane Smith").build();
        event1.setInitiator(newInitiator);
        assertEquals(newInitiator, event1.getInitiator(), "Сеттер initiator должен работать корректно");

        LocationDto newLocation = LocationDto.builder().lat(59.9343f).lon(30.3351f).build();
        event1.setLocation(newLocation);
        assertEquals(newLocation, event1.getLocation(), "Сеттер location должен работать корректно");
    }

    @Test
    void eventShortDto_WithNoArgsConstructor_ShouldCreateObject() {
        // Given & When
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test Event");
        eventShortDto.setAnnotation("Test annotation");
        eventShortDto.setEventDate(LocalDateTime.now().plusDays(5));
        eventShortDto.setPaid(true);
        eventShortDto.setConfirmedRequests(50L);
        eventShortDto.setViews(100L);

        // Then
        assertNotNull(eventShortDto);
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Test Event", eventShortDto.getTitle());
        assertEquals("Test annotation", eventShortDto.getAnnotation());
        assertTrue(eventShortDto.getPaid());
        assertEquals(50L, eventShortDto.getConfirmedRequests());
        assertEquals(100L, eventShortDto.getViews());
    }

    @Test
    void eventShortDto_WithAllArgsConstructor_ShouldCreateObject() {
        // Given & When
        LocalDateTime eventDate = LocalDateTime.now().plusDays(5);
        CategoryDto category = CategoryDto.builder().id(1L).name("Concerts").build();
        UserShortDto initiator = UserShortDto.builder().id(1L).name("John Doe").build();
        LocationDto location = LocationDto.builder().lat(55.7558f).lon(37.6173f).build();

        EventShortDto eventShortDto = new EventShortDto(
                1L, "Test annotation", category, 50L, eventDate,
                initiator, true, "Test Event", location, 100L
        );

        // Then
        assertNotNull(eventShortDto);
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Test Event", eventShortDto.getTitle());
        assertEquals("Test annotation", eventShortDto.getAnnotation());
        assertEquals(eventDate, eventShortDto.getEventDate());
        assertTrue(eventShortDto.getPaid());
        assertEquals(50L, eventShortDto.getConfirmedRequests());
        assertEquals(100L, eventShortDto.getViews());
        assertEquals(category, eventShortDto.getCategory());
        assertEquals(initiator, eventShortDto.getInitiator());
        assertEquals(location, eventShortDto.getLocation());
    }

    @Test
    void eventShortDto_ShouldHandleJsonFormatForEventDate() throws JsonProcessingException {
        // Given
        String eventDate = "2024-07-15 19:00:00";

        String json = String.format("{" +
                "\"id\":1," +
                "\"title\":\"Test Event\"," +
                "\"eventDate\":\"%s\"" +
                "}", eventDate);

        // When
        EventShortDto eventShortDto = objectMapper.readValue(json, EventShortDto.class);

        // Then - дата должна быть корректно распарсена
        assertNotNull(eventShortDto);
        assertEquals(LocalDateTime.parse(eventDate, formatter), eventShortDto.getEventDate());
    }

    @Test
    void eventShortDto_ShouldIgnoreUnknownProperties() throws JsonProcessingException {
        // Given
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"eventDate\":\"2024-07-15 19:00:00\"," +
                "\"unknownField\":\"some value\"," +
                "\"anotherUnknown\":123" +
                "}";

        // When
        EventShortDto eventShortDto = objectMapper.readValue(json, EventShortDto.class);

        // Then - неизвестные поля должны быть проигнорированы
        assertNotNull(eventShortDto);
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Summer Concert", eventShortDto.getTitle());
        assertEquals(LocalDateTime.of(2024, 7, 15, 19, 0, 0),
                eventShortDto.getEventDate());
    }

    @Test
    void eventShortDto_ShouldHandleBooleanPaidField() throws JsonProcessingException {
        // Given
        String jsonTrue = "{\"id\":1,\"title\":\"Event\",\"eventDate\":\"2024-07-15 19:00:00\",\"paid\":true}";
        String jsonFalse = "{\"id\":2,\"title\":\"Event\",\"eventDate\":\"2024-07-15 19:00:00\",\"paid\":false}";

        // When
        EventShortDto eventWithPaidTrue = objectMapper.readValue(jsonTrue, EventShortDto.class);
        EventShortDto eventWithPaidFalse = objectMapper.readValue(jsonFalse, EventShortDto.class);

        // Then
        assertTrue(eventWithPaidTrue.getPaid());
        assertFalse(eventWithPaidFalse.getPaid());
    }
}