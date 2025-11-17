package ru.practicum.main.controller.privates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainServiceStart.class)
@ActiveProfiles("test")
class PrivateCommentControllerIntegrationTest {

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
    private Comment testComment;

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

        // Создаем тестовый комментарий
        testComment = new Comment();
        testComment.setText("Test comment text");
        testComment.setAuthor(testUser);
        testComment.setEvent(testEvent);
        testComment.setState(CommentStatus.PENDING);
        testComment.setCreated(LocalDateTime.now());
        testComment.setUpdated(LocalDateTime.now());
        testComment = commentRepository.save(testComment);
    }

    @Test
    void saveComment_whenValid_thenReturnCreatedComment() throws Exception {
        String requestBody = "{\"text\": \"New comment\"}";

        mockMvc.perform(post("/users/{userId}/comments/events/{eventId}", testUser.getId(), testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New comment"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void saveComment_whenInvalidText_thenReturnBadRequest() throws Exception {
        String requestBody = "{\"text\": \"\"}";

        mockMvc.perform(post("/users/{userId}/comments/events/{eventId}", testUser.getId(), testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveComment_whenTextTooLong_thenReturnBadRequest() throws Exception {
        String longText = "a".repeat(256);
        String requestBody = "{\"text\": \"" + longText + "\"}";

        mockMvc.perform(post("/users/{userId}/comments/events/{eventId}", testUser.getId(), testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveComment_whenUserNotFound_thenReturnNotFound() throws Exception {
        String requestBody = "{\"text\": \"New comment\"}";

        mockMvc.perform(post("/users/{userId}/comments/events/{eventId}", 999L, testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveComment_whenEventNotFound_thenReturnNotFound() throws Exception {
        String requestBody = "{\"text\": \"New comment\"}";

        mockMvc.perform(post("/users/{userId}/comments/events/{eventId}", testUser.getId(), 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserComment_whenExists_thenReturnComment() throws Exception {
        mockMvc.perform(get("/users/{userId}/comments/{commentId}", testUser.getId(), testComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testComment.getId()))
                .andExpect(jsonPath("$.text").value("Test comment text"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void getUserComment_whenCommentNotFound_thenReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/{userId}/comments/{commentId}", testUser.getId(), 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserComments_whenCommentsExist_thenReturnCommentsList() throws Exception {
        mockMvc.perform(get("/users/{userId}/comments", testUser.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Test comment text"))
                .andExpect(jsonPath("$[0].state").value("PENDING"));
    }

    @Test
    void getUserComments_whenNoComments_thenReturnEmptyList() throws Exception {
        mockMvc.perform(get("/users/{userId}/comments", anotherUser.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateComment_whenValid_thenReturnUpdatedComment() throws Exception {
        String requestBody = "{\"text\": \"Updated comment\"}";

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", testUser.getId(), testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment"))
                .andExpect(jsonPath("$.state").value("EDITED"));
    }

    @Test
    void updateComment_whenUserIsNotAuthor_thenReturnForbidden() throws Exception {
        String requestBody = "{\"text\": \"Updated comment\"}";

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", anotherUser.getId(), testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void updateComment_whenCommentNotFound_thenReturnNotFound() throws Exception {
        String requestBody = "{\"text\": \"Updated comment\"}";

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", testUser.getId(), 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_whenValid_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", testUser.getId(), testComment.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_whenUserIsNotAuthor_thenReturnForbidden() throws Exception {
        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", anotherUser.getId(), testComment.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteComment_whenCommentNotFound_thenReturnNotFound() throws Exception {
        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", testUser.getId(), 999L))
                .andExpect(status().isNotFound());
    }
}