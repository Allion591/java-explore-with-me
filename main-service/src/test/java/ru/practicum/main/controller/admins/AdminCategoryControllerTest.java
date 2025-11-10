package ru.practicum.main.controller.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.service.interfaces.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void createCategory_ShouldReturnCreated() throws Exception {
        NewCategoryDto request = new NewCategoryDto("Концерты");
        CategoryDto response = new CategoryDto(1L, "Концерты");

        when(categoryService.createCategory(any())).thenReturn(response);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCategory_ShouldReturnOk() throws Exception {
        CategoryDto request = new CategoryDto(1L, "Обновленное название");
        when(categoryService.updateCategory(any(), any())).thenReturn(request);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType("application/json")
                        .content("{\"name\": \"Новое название\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленное название"));
    }
}