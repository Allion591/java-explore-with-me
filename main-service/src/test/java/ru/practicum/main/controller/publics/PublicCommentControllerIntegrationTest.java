package ru.practicum.main.controller.publics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.main.MainServiceStart;
import ru.practicum.main.enums.CommentStatus;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.CommentsRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainServiceStart.class)
@ActiveProfiles("test")
class PublicCommentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CommentsRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private User testUser;
    private User anotherUser;
    private Category testCategory;
    private Event testEvent;
    private Event anotherEvent;
    private Comment approvedComment;
    private Comment pendingComment;
    private Comment rejectedComment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearDatabase();
        createTestData();
    }

    private void clearDatabase() {
        commentRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createTestData() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@email.com");
        testUser.setCreatedOn(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Создаем второго пользователя
        anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@email.com");
        anotherUser.setCreatedOn(LocalDateTime.now());
        anotherUser = userRepository.save(anotherUser);

        // Создаем тестовую категорию
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory = categoryRepository.save(testCategory);

        Location location = Location.builder()
                .lat(55.7558f)
                .lon(37.6173f)
                .build();

        // Создаем тестовое событие
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setAnnotation("Test Annotation");
        testEvent.setDescription("Test Description");
        testEvent.setCategory(testCategory);
        testEvent.setInitiator(testUser);
        testEvent.setEventDate(LocalDateTime.now().plusDays(1));
        testEvent.setCreatedOn(LocalDateTime.now());
        testEvent.setState(EventState.PUBLISHED);
        testEvent.setParticipantLimit(0);
        testEvent.setRequestModeration(false);
        testEvent.setPaid(false);
        testEvent.setLocation(location);
        testEvent = eventRepository.save(testEvent);

        // Создаем второе событие
        anotherEvent = new Event();
        anotherEvent.setTitle("Another Event");
        anotherEvent.setAnnotation("Another Annotation");
        anotherEvent.setDescription("Another Description");
        anotherEvent.setCategory(testCategory);
        anotherEvent.setInitiator(anotherUser);
        anotherEvent.setEventDate(LocalDateTime.now().plusDays(2));
        anotherEvent.setCreatedOn(LocalDateTime.now());
        anotherEvent.setState(EventState.PUBLISHED);
        anotherEvent.setParticipantLimit(0);
        anotherEvent.setRequestModeration(false);
        anotherEvent.setPaid(false);
        anotherEvent.setLocation(location);
        anotherEvent = eventRepository.save(anotherEvent);

        // Создаем одобренный комментарий
        approvedComment = new Comment();
        approvedComment.setText("Approved comment text");
        approvedComment.setAuthor(testUser);
        approvedComment.setEvent(testEvent);
        approvedComment.setState(CommentStatus.APPROVED);
        approvedComment.setCreated(LocalDateTime.now().minusHours(2));
        approvedComment.setUpdated(LocalDateTime.now().minusHours(1));
        approvedComment = commentRepository.save(approvedComment);

        // Создаем комментарий на модерации
        pendingComment = new Comment();
        pendingComment.setText("Pending comment text");
        pendingComment.setAuthor(anotherUser);
        pendingComment.setEvent(testEvent);
        pendingComment.setState(CommentStatus.PENDING);
        pendingComment.setCreated(LocalDateTime.now().minusHours(1));
        pendingComment.setUpdated(LocalDateTime.now());
        pendingComment = commentRepository.save(pendingComment);

        // Создаем отклоненный комментарий
        rejectedComment = new Comment();
        rejectedComment.setText("Rejected comment text");
        rejectedComment.setAuthor(testUser);
        rejectedComment.setEvent(testEvent);
        rejectedComment.setState(CommentStatus.REJECTED);
        rejectedComment.setCreated(LocalDateTime.now().minusHours(3));
        rejectedComment.setUpdated(LocalDateTime.now().minusHours(2));
        rejectedComment = commentRepository.save(rejectedComment);

        // Создаем комментарий для другого события
        Comment anotherEventComment = new Comment();
        anotherEventComment.setText("Another event comment");
        anotherEventComment.setAuthor(anotherUser);
        anotherEventComment.setEvent(anotherEvent);
        anotherEventComment.setState(CommentStatus.APPROVED);
        anotherEventComment.setCreated(LocalDateTime.now().minusHours(1));
        anotherEventComment.setUpdated(LocalDateTime.now());
        commentRepository.save(anotherEventComment);
    }

    @Test
    void getAllCommentsByEventId_whenCommentsExist_thenReturnOnlyApprovedComments() throws Exception {
        mockMvc.perform(get("/comments/{eventId}", testEvent.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Только APPROVED комментарии
                .andExpect(jsonPath("$[0].text").value("Approved comment text"))
                .andExpect(jsonPath("$[0].state").value("APPROVED"));
    }

    @Test
    void getAllCommentsByEventId_whenNoApprovedComments_thenReturnEmptyList() throws Exception {
        // Удаляем все комментарии и создаем только неодобренные
        commentRepository.deleteAll();

        Comment pending = new Comment();
        pending.setText("Only pending comment");
        pending.setAuthor(testUser);
        pending.setEvent(testEvent);
        pending.setState(CommentStatus.PENDING);
        pending.setCreated(LocalDateTime.now());
        commentRepository.save(pending);

        Comment rejected = new Comment();
        rejected.setText("Only rejected comment");
        rejected.setAuthor(anotherUser);
        rejected.setEvent(testEvent);
        rejected.setState(CommentStatus.REJECTED);
        rejected.setCreated(LocalDateTime.now());
        commentRepository.save(rejected);

        mockMvc.perform(get("/comments/{eventId}", testEvent.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); // Пустой список, так как нет APPROVED
    }

    @Test
    void getAllCommentsByEventId_whenEventHasNoComments_thenReturnEmptyList() throws Exception {
        // Создаем новое событие без комментариев
        Event newEvent = new Event();
        newEvent.setTitle("New Event Without Comments");
        newEvent.setAnnotation("New Annotation");
        newEvent.setDescription("New Description");
        newEvent.setCategory(testCategory);
        newEvent.setInitiator(testUser);
        newEvent.setEventDate(LocalDateTime.now().plusDays(3));
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setState(EventState.PUBLISHED);
        newEvent.setParticipantLimit(0);
        newEvent.setRequestModeration(false);
        newEvent.setPaid(false);
        newEvent.setLocation(Location.builder().lat(55.7558f).lon(37.6173f).build());
        newEvent = eventRepository.save(newEvent);

        mockMvc.perform(get("/comments/{eventId}", newEvent.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllCommentsByEventId_whenEventNotFound_thenReturnNotFound() throws Exception {
        mockMvc.perform(get("/comments/{eventId}", 999L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCommentsByEventId_whenInvalidFrom_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/comments/{eventId}", testEvent.getId())
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCommentsByEventId_whenSizeTooLarge_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/comments/{eventId}", testEvent.getId())
                        .param("from", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCommentsByEventId_whenInvalidEventId_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/comments/{eventId}", 0)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}