package ru.practicum.main.controller.publics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.service.interfaces.CategoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicCategoryControllerUnitTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private PublicCategoryController publicCategoryController;

    @Test
    void getCategories_ShouldReturnCategoryList() {
        Integer from = 0;
        Integer size = 10;

        CategoryDto category1 = CategoryDto.builder()
                .id(1L)
                .name("Concerts")
                .build();

        CategoryDto category2 = CategoryDto.builder()
                .id(2L)
                .name("Festivals")
                .build();

        when(categoryService.getCategories(anyInt(), anyInt())).thenReturn(List.of(category1, category2));

        List<CategoryDto> result = publicCategoryController.getCategories(from, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Concerts", result.get(0).getName());
        assertEquals("Festivals", result.get(1).getName());
        verify(categoryService, times(1)).getCategories(from, size);
    }

    @Test
    void getCategory_ShouldReturnCategory() {
        Long categoryId = 1L;
        CategoryDto expectedCategory = CategoryDto.builder()
                .id(categoryId)
                .name("Concerts")
                .build();

        when(categoryService.getCategory(anyLong())).thenReturn(expectedCategory);

        CategoryDto actualCategory = publicCategoryController.getCategory(categoryId);

        assertNotNull(actualCategory);
        assertEquals(categoryId, actualCategory.getId());
        assertEquals("Concerts", actualCategory.getName());
        verify(categoryService, times(1)).getCategory(categoryId);
    }
}