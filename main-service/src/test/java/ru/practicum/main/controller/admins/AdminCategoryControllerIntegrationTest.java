package ru.practicum.main.admins;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AdminCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createCategory_ShouldWork() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .contentType("application/json")
                        .content("{\"name\": \"Концерты\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    void createDuplicateCategory_ShouldReturnConflict() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .contentType("application/json")
                        .content("{\"name\": \"Дубликат\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/admin/categories")
                        .contentType("application/json")
                        .content("{\"name\": \"Дубликат\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_ShouldWork() throws Exception {
        String response = mockMvc.perform(post("/admin/categories")
                        .contentType("application/json")
                        .content("{\"name\": \"Для удаления\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = response.split("\"id\":")[1].split(",")[0].trim();

        mockMvc.perform(delete("/admin/categories/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCategory_ShouldWork() throws Exception {
        String response = mockMvc.perform(post("/admin/categories")
                        .contentType("application/json")
                        .content("{\"name\": \"Старое название\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = response.split("\"id\":")[1].split(",")[0].trim();

        mockMvc.perform(patch("/admin/categories/" + id)
                        .contentType("application/json")
                        .content("{\"name\": \"Новое название\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое название"));
    }
}