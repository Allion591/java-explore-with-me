package ru.practicum.main.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryModelTest {

    @Test
    void category_ShouldCreateWithBuilder() {
        // Given & When
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        // Then
        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Концерты", category.getName());
        assertNull(category.getEvents());
    }

    @Test
    void category_ShouldHaveNoArgsConstructor() {
        // Given & When
        Category category = new Category();
        category.setId(1L);
        category.setName("Фестивали");

        // Then
        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Фестивали", category.getName());
    }

    @Test
    void category_ShouldHaveAllArgsConstructor() {
        // Given & When
        Category category = new Category(1L, "Выставки", null);

        // Then
        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Выставки", category.getName());
        assertNull(category.getEvents());
    }

    @Test
    void category_ShouldHaveLombokFunctionality() {
        // Given & When
        Category category1 = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Category category2 = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Category category3 = Category.builder()
                .id(2L)
                .name("Фестивали")
                .build();

        // Then
        assertEquals(category1, category2);
        assertNotEquals(category1, category3);
        assertEquals(category1.hashCode(), category2.hashCode());
        assertNotEquals(category1.hashCode(), category3.hashCode());

        assertNotNull(category1.toString());
        assertTrue(category1.toString().contains("Концерты"));
    }

    @Test
    void category_ShouldHandleEventsList() {
        // Given
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        // When
        category.setEvents(java.util.Collections.emptyList());

        // Then
        assertNotNull(category.getEvents());
        assertTrue(category.getEvents().isEmpty());
    }
}