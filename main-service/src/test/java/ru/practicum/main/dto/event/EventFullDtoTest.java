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
class EventFullDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void eventFullDto_ShouldSerializeToJson() throws JsonProcessingException {
        EventFullDto eventFullDto = EventFullDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great summer concert event")
                .description("Detailed description of the summer concert event")
                .eventDate(LocalDateTime.of(2024, 7, 15, 19, 0, 0))
                .createdOn(LocalDateTime.of(2024, 6, 1, 10, 0, 0))
                .publishedOn(LocalDateTime.of(2024, 6, 2, 12, 0, 0))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state("PUBLISHED")
                .confirmedRequests(50L)
                .views(150L)
                .category(CategoryDto.builder().id(1L).name("Concerts").build())
                .initiator(UserShortDto.builder().id(1L).name("John Doe").build())
                .location(LocationDto.builder().lat(55.7558f).lon(37.6173f).build())
                .build();

        String json = objectMapper.writeValueAsString(eventFullDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"title\":\"Summer Concert\""));
        assertTrue(json.contains("\"annotation\":\"Great summer concert event\""));
        assertTrue(json.contains("\"participantLimit\":100"));
        assertTrue(json.contains("\"state\":\"PUBLISHED\""));
        assertTrue(json.contains("\"confirmedRequests\":50"));
        assertTrue(json.contains("\"views\":150"));
        assertTrue(json.contains("\"eventDate\":\"2024-07-15 19:00:00\""));
        assertTrue(json.contains("\"createdOn\":\"2024-06-01 10:00:00\""));
        assertTrue(json.contains("\"publishedOn\":\"2024-06-02 12:00:00\""));
    }

    @Test
    void eventFullDto_ShouldDeserializeFromJson() throws JsonProcessingException {
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"annotation\":\"Great summer concert event\"," +
                "\"description\":\"Detailed description\"," +
                "\"eventDate\":\"2024-07-15 19:00:00\"," +
                "\"createdOn\":\"2024-06-01 10:00:00\"," +
                "\"publishedOn\":\"2024-06-02 12:00:00\"," +
                "\"paid\":true," +
                "\"participantLimit\":100," +
                "\"requestModeration\":true," +
                "\"state\":\"PUBLISHED\"," +
                "\"confirmedRequests\":50," +
                "\"views\":150," +
                "\"category\":{\"id\":1,\"name\":\"Concerts\"}," +
                "\"initiator\":{\"id\":1,\"name\":\"John Doe\"}," +
                "\"location\":{\"lat\":55.7558,\"lon\":37.6173}" +
                "}";

        EventFullDto eventFullDto = objectMapper.readValue(json, EventFullDto.class);

        assertNotNull(eventFullDto);
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Summer Concert", eventFullDto.getTitle());
        assertEquals("Great summer concert event", eventFullDto.getAnnotation());
        assertEquals("Detailed description", eventFullDto.getDescription());
        assertEquals(LocalDateTime.of(2024, 7, 15, 19, 0, 0), eventFullDto.getEventDate());
        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 0, 0), eventFullDto.getCreatedOn());
        assertEquals(LocalDateTime.of(2024, 6, 2, 12, 0, 0), eventFullDto.getPublishedOn());
        assertTrue(eventFullDto.getPaid());
        assertEquals(100, eventFullDto.getParticipantLimit());
        assertTrue(eventFullDto.getRequestModeration());
        assertEquals("PUBLISHED", eventFullDto.getState());
        assertEquals(50L, eventFullDto.getConfirmedRequests());
        assertEquals(150L, eventFullDto.getViews());

        assertNotNull(eventFullDto.getCategory());
        assertEquals(1L, eventFullDto.getCategory().getId());
        assertEquals("Concerts", eventFullDto.getCategory().getName());

        assertNotNull(eventFullDto.getInitiator());
        assertEquals(1L, eventFullDto.getInitiator().getId());
        assertEquals("John Doe", eventFullDto.getInitiator().getName());

        assertNotNull(eventFullDto.getLocation());
        assertEquals(55.7558f, eventFullDto.getLocation().getLat(), 0.0001f);
        assertEquals(37.6173f, eventFullDto.getLocation().getLon(), 0.0001f);
    }

    @Test
    void eventFullDto_ShouldUseDefaultParticipantLimit() throws JsonProcessingException {
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"annotation\":\"Great event\"," +
                "\"eventDate\":\"2024-07-15 19:00:00\"," +
                "\"paid\":true," +
                "\"state\":\"PENDING\"" +
                "}";

        EventFullDto eventFullDto = objectMapper.readValue(json, EventFullDto.class);

        assertNotNull(eventFullDto);
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Summer Concert", eventFullDto.getTitle());
        assertEquals(0, eventFullDto.getParticipantLimit());
    }

    @Test
    void eventFullDto_ShouldHandleNullValues() throws JsonProcessingException {
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Summer Concert\"," +
                "\"annotation\":null," +
                "\"description\":null," +
                "\"eventDate\":\"2024-07-15 19:00:00\"," +
                "\"paid\":null," +
                "\"participantLimit\":null," +
                "\"state\":\"PENDING\"," +
                "\"category\":null," +
                "\"initiator\":null," +
                "\"location\":null" +
                "}";

        EventFullDto eventFullDto = objectMapper.readValue(json, EventFullDto.class);

        assertNotNull(eventFullDto);
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Summer Concert", eventFullDto.getTitle());
        assertNull(eventFullDto.getAnnotation());
        assertNull(eventFullDto.getDescription());
        assertNull(eventFullDto.getPaid());
        assertNull(eventFullDto.getParticipantLimit()); // null переопределяет значение по умолчанию
        assertNull(eventFullDto.getCategory());
        assertNull(eventFullDto.getInitiator());
        assertNull(eventFullDto.getLocation());
    }

    @Test
    void eventFullDto_ShouldHaveLombokFunctionality() {
        EventFullDto event1 = EventFullDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great event")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .participantLimit(100)
                .state("PUBLISHED")
                .build();

        EventFullDto event2 = EventFullDto.builder()
                .id(1L)
                .title("Summer Concert")
                .annotation("Great event")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .participantLimit(100)
                .state("PUBLISHED")
                .build();

        EventFullDto event3 = EventFullDto.builder()
                .id(2L)
                .title("Winter Festival")
                .annotation("Winter event")
                .eventDate(LocalDateTime.now().plusDays(10))
                .paid(false)
                .participantLimit(50)
                .state("PENDING")
                .build();

        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1.hashCode(), event3.hashCode());

        assertNotNull(event1.toString());
        assertTrue(event1.toString().contains("Summer Concert"));

        event1.setTitle("Updated Title");
        assertEquals("Updated Title", event1.getTitle());

        event1.setPaid(false);
        assertFalse(event1.getPaid());

        event1.setParticipantLimit(200);
        assertEquals(200, event1.getParticipantLimit());

        event1.setState("CANCELED");
        assertEquals("CANCELED", event1.getState());

        CategoryDto category = CategoryDto.builder().id(2L).name("Festivals").build();
        event1.setCategory(category);
        assertEquals(category, event1.getCategory());
    }

    @Test
    void eventFullDto_ShouldUseBuilderDefaultForParticipantLimit() {
        EventFullDto eventFullDto = EventFullDto.builder()
                .id(1L)
                .title("Test Event")
                .annotation("Test annotation")
                .eventDate(LocalDateTime.now().plusDays(5))
                .paid(true)
                .state("PENDING")
                .build();

        assertNotNull(eventFullDto);
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Test Event", eventFullDto.getTitle());
        assertEquals(0, eventFullDto.getParticipantLimit());
    }

    @Test
    void eventFullDto_WithNoArgsConstructor_ShouldCreateObject() {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event");
        eventFullDto.setAnnotation("Test annotation");
        eventFullDto.setEventDate(LocalDateTime.now().plusDays(5));
        eventFullDto.setPaid(true);
        eventFullDto.setParticipantLimit(100);
        eventFullDto.setState("PUBLISHED");

        assertNotNull(eventFullDto);
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Test Event", eventFullDto.getTitle());
        assertEquals(100, eventFullDto.getParticipantLimit());
        assertEquals("PUBLISHED", eventFullDto.getState());
    }

    @Test
    void eventFullDto_WithAllArgsConstructor_ShouldCreateObject() {
        LocalDateTime now = LocalDateTime.now();
        CategoryDto category = CategoryDto.builder().id(1L).name("Concerts").build();
        UserShortDto initiator = UserShortDto.builder().id(1L).name("John Doe").build();
        LocationDto location = LocationDto.builder().lat(55.7558f).lon(37.6173f).build();

        EventFullDto eventFullDto = new EventFullDto(
                1L, "Test annotation", category, 50L, now, "Test description",
                now.plusDays(5), initiator, location, true, 100, now.plusDays(1),
                true, "PUBLISHED", "Test Event", 150L
        );

        assertNotNull(eventFullDto);
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Test Event", eventFullDto.getTitle());
        assertEquals("Test annotation", eventFullDto.getAnnotation());
        assertEquals("Test description", eventFullDto.getDescription());
        assertEquals(100, eventFullDto.getParticipantLimit());
        assertEquals("PUBLISHED", eventFullDto.getState());
        assertEquals(50L, eventFullDto.getConfirmedRequests());
        assertEquals(150L, eventFullDto.getViews());
        assertEquals(category, eventFullDto.getCategory());
        assertEquals(initiator, eventFullDto.getInitiator());
        assertEquals(location, eventFullDto.getLocation());
    }

    @Test
    void eventFullDto_ShouldHandleJsonFormatForDates() throws JsonProcessingException {
        String eventDate = "2024-07-15 19:00:00";
        String createdOn = "2024-06-01 10:00:00";
        String publishedOn = "2024-06-02 12:00:00";

        String json = String.format("{" +
                "\"id\":1," +
                "\"title\":\"Test Event\"," +
                "\"eventDate\":\"%s\"," +
                "\"createdOn\":\"%s\"," +
                "\"publishedOn\":\"%s\"," +
                "\"state\":\"PUBLISHED\"" +
                "}", eventDate, createdOn, publishedOn);

        EventFullDto eventFullDto = objectMapper.readValue(json, EventFullDto.class);

        assertNotNull(eventFullDto);
        assertEquals(LocalDateTime.parse(eventDate, formatter), eventFullDto.getEventDate());
        assertEquals(LocalDateTime.parse(createdOn, formatter), eventFullDto.getCreatedOn());
        assertEquals(LocalDateTime.parse(publishedOn, formatter), eventFullDto.getPublishedOn());
    }

    @Test
    void eventFullDto_JsonPropertyForParticipantLimit() throws JsonProcessingException {
        String json = "{" +
                "\"id\":1," +
                "\"title\":\"Test Event\"," +
                "\"participantLimit\":200," + // Используем имя из @JsonProperty
                "\"state\":\"PUBLISHED\"" +
                "}";

        EventFullDto eventFullDto = objectMapper.readValue(json, EventFullDto.class);

        assertNotNull(eventFullDto);
        assertEquals(200, eventFullDto.getParticipantLimit());
    }
}