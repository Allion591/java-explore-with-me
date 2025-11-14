package ru.practicum.main.controller.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.service.interfaces.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ShouldReturn201AndUserDto() throws Exception {
        NewUserRequest request = new NewUserRequest("John Doe", "john@example.com");
        UserDto response = new UserDto(1L, "John Doe", "john@example.com");

        when(userService.createUser(any(NewUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    void getUsers_WithIds_ShouldReturn200AndUserList() throws Exception {
        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Smith", "jane@example.com");

        when(userService.getUsers(anyList(), anyInt(), anyInt())).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(userService, times(1)).getUsers(anyList(), anyInt(), anyInt());
    }

    @Test
    void getUsers_WithoutIds_ShouldReturn200AndAllUsers() throws Exception {
        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Smith", "jane@example.com");

        when(userService.getUsers(isNull(), anyInt(), anyInt())).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService, times(1)).getUsers(isNull(), anyInt(), anyInt());
    }

    @Test
    void getUsers_WithInvalidPagination_ShouldReturn400() throws Exception {
        Integer invalidFrom = -1;
        Integer invalidSize = 0;

        mockMvc.perform(get("/admin/users")
                        .param("from", invalidFrom.toString())
                        .param("size", invalidSize.toString()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUsers(any(), anyInt(), anyInt());
    }

    @Test
    void deleteUser_ShouldReturn204() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldReturn400() throws Exception {
        //- невалидный ID
        Long invalidUserId = 0L;

        mockMvc.perform(delete("/admin/users/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());

        verify(userService, never()).deleteUser(anyLong());
    }
}