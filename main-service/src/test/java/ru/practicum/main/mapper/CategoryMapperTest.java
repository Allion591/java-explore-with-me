package ru.practicum.main.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.model.Category;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void toCategoryDto_ShouldMapCategoryToCategoryDto() {
        // Given
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        // When
        CategoryDto categoryDto = categoryMapper.toCategoryDto(category);

        // Then
        assertNotNull(categoryDto);
        assertEquals(category.getId(), categoryDto.getId());
        assertEquals(category.getName(), categoryDto.getName());
    }

    @Test
    void toCategory_ShouldMapNewCategoryDtoToCategory() {
        // Given
        NewCategoryDto newCategoryDto = NewCategoryDto.builder()
                .name("Фестивали")
                .build();

        // When
        Category category = categoryMapper.toCategory(newCategoryDto);

        // Then
        assertNotNull(category);
        assertNull(category.getId()); // id должен быть проигнорирован
        assertNull(category.getEvents()); // events должен быть проигнорирован
        assertEquals(newCategoryDto.getName(), category.getName());
    }

    @Test
    void toCategory_ShouldHandleNull() {
        // When
        Category category = categoryMapper.toCategory(null);

        // Then
        assertNull(category);
    }

    @Test
    void toCategoryDto_ShouldHandleNull() {
        // When
        CategoryDto categoryDto = categoryMapper.toCategoryDto(null);

        // Then
        assertNull(categoryDto);
    }
}