package ru.practicum.main.controller.admins;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainServiceStart.class)
@ActiveProfiles("test")
class AdminCommentControllerIntegrationTest {

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
    void updateStatusComment_whenCommentExists_thenUpdateAndReturnComment() throws Exception {
        String requestBody = "{\"stateAction\": \"APPROVE\"}";

        mockMvc.perform(patch("/admin/comments/{commentId}", testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testComment.getId()))
                .andExpect(jsonPath("$.text").value("Test comment text"))
                .andExpect(jsonPath("$.state").value("APPROVED"));
    }

    @Test
    void updateStatusComment_whenRejectComment_thenUpdateStateToRejected() throws Exception {
        String requestBody = "{\"stateAction\": \"REJECT\"}";

        mockMvc.perform(patch("/admin/comments/{commentId}", testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REJECTED"));
    }

    @Test
    void updateStatusComment_whenCommentNotFound_thenReturnNotFound() throws Exception {
        String requestBody = "{\"stateAction\": \"PUBLISH_COMMENT\"}";

        mockMvc.perform(patch("/admin/comments/{commentId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPendingComments_whenCommentsExist_thenReturnCommentsList() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Test comment text"))
                .andExpect(jsonPath("$[0].state").value("PENDING"));
    }

    @Test
    void getAllPendingComments_whenNoComments_thenReturnEmptyList() throws Exception {
        // Удаляем все комментарии перед тестом
        commentRepository.deleteAll();

        mockMvc.perform(get("/admin/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllPendingComments_whenInvalidPage_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllPendingComments_whenInvalidSize_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllPendingComments_whenSizeTooLarge_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusComment_whenInvalidStateAction_thenReturnBadRequest() throws Exception {
        String requestBody = "{\"stateAction\": \"INVALID_ACTION\"}";

        mockMvc.perform(patch("/admin/comments/{commentId}", testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}