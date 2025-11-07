package ru.practicum.main.controller.publics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.service.interfaces.CategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getCategories_ShouldReturn200AndCategoryList() throws Exception {
        CategoryDto category1 = CategoryDto.builder()
                .id(1L)
                .name("Concerts")
                .build();

        CategoryDto category2 = CategoryDto.builder()
                .id(2L)
                .name("Festivals")
                .build();

        when(categoryService.getCategories(anyInt(), anyInt())).thenReturn(List.of(category1, category2));

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Concerts"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Festivals"));

        verify(categoryService, times(1)).getCategories(0, 10);
    }

    @Test
    void getCategories_WithDefaultPagination_ShouldReturn200() throws Exception {
        when(categoryService.getCategories(anyInt(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).getCategories(0, 10);
    }

    @Test
    void getCategories_WithInvalidPagination_ShouldReturn400() throws Exception {
        Integer invalidFrom = -1;
        Integer invalidSize = 0;

        mockMvc.perform(get("/categories")
                        .param("from", invalidFrom.toString())
                        .param("size", invalidSize.toString()))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategories(anyInt(), anyInt());
    }

    @Test
    void getCategory_ShouldReturn200AndCategory() throws Exception {
        Long categoryId = 1L;
        CategoryDto category = CategoryDto.builder()
                .id(categoryId)
                .name("Concerts")
                .build();

        when(categoryService.getCategory(anyLong())).thenReturn(category);

        mockMvc.perform(get("/categories/{catId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Concerts"));

        verify(categoryService, times(1)).getCategory(categoryId);
    }

    @Test
    void getCategory_WithInvalidId_ShouldReturn400() throws Exception {
        Long invalidCategoryId = 0L;

        mockMvc.perform(get("/categories/{catId}", invalidCategoryId))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategory(anyLong());
    }
}