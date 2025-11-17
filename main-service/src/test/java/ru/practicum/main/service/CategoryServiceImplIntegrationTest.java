package ru.practicum.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.exception.conflict.CategoryNotEmptyException;
import ru.practicum.main.exception.notFound.CategoryNotFoundException;
import ru.practicum.main.exception.validation.CategoryNameConflictException;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.implementations.CategoryServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceImplIntegrationTest {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Category existingCategory;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        categoryRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = User.builder()
                .name("Test User")
                .email("test@email.com")
                .build();
        testUser = userRepository.save(testUser);

        // Создаем существующую категорию
        existingCategory = Category.builder()
                .name("Existing Category")
                .build();
        existingCategory = categoryRepository.save(existingCategory);
    }

    @Test
    void createCategory_shouldCreateCategorySuccessfully() {
        NewCategoryDto newCategoryDto = new NewCategoryDto("New Category");

        CategoryDto result = categoryService.createCategory(newCategoryDto);

        assertNotNull(result.getId());
        assertEquals("New Category", result.getName());

        // Проверяем, что категория сохранена в БД
        Category savedCategory = categoryRepository.findById(result.getId()).orElseThrow();
        assertEquals("New Category", savedCategory.getName());
    }

    @Test
    void deleteCategory_shouldDeleteCategorySuccessfully() {
        categoryService.deleteCategory(existingCategory.getId());

        assertFalse(categoryRepository.existsById(existingCategory.getId()));
    }

    @Test
    void deleteCategory_whenCategoryUsedInEvent_shouldThrowException() {
        // Создаем событие, связанное с категорией
        Event event = Event.builder()
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .title("Test Event")
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .initiator(testUser)
                .category(existingCategory)
                .state(ru.practicum.main.enums.EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .views(0L)
                .confirmedRequests(0L)
                .location(new ru.practicum.main.model.Location(55.7558f, 37.6173f))
                .build();
        eventRepository.save(event);

        assertThrows(CategoryNotEmptyException.class, () -> {
            categoryService.deleteCategory(existingCategory.getId());
        });
    }

    @Test
    void deleteCategory_withNonExistentId_shouldThrowException() {
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.deleteCategory(999L);
        });
    }

    @Test
    void updateCategory_shouldUpdateCategorySuccessfully() {
        CategoryDto updateDto = new CategoryDto(existingCategory.getId(), "Updated Category");

        CategoryDto result = categoryService.updateCategory(existingCategory.getId(), updateDto);

        assertEquals(existingCategory.getId(), result.getId());
        assertEquals("Updated Category", result.getName());

        // Проверяем, что категория обновлена в БД
        Category updatedCategory = categoryRepository.findById(existingCategory.getId()).orElseThrow();
        assertEquals("Updated Category", updatedCategory.getName());
    }

    @Test
    void updateCategory_withDuplicateName_shouldThrowException() {
        // Создаем вторую категорию
        Category anotherCategory = Category.builder()
                .name("Another Category")
                .build();
        categoryRepository.save(anotherCategory);

        // Пытаемся переименовать существующую категорию в имя второй категории
        CategoryDto updateDto = new CategoryDto(existingCategory.getId(), "Another Category");

        assertThrows(CategoryNameConflictException.class, () -> {
            categoryService.updateCategory(existingCategory.getId(), updateDto);
        });
    }

    @Test
    void updateCategory_withSameName_shouldUpdateSuccessfully() {
        // Обновление с тем же именем должно быть разрешено
        CategoryDto updateDto = new CategoryDto(existingCategory.getId(), "Existing Category");

        CategoryDto result = categoryService.updateCategory(existingCategory.getId(), updateDto);

        assertEquals("Existing Category", result.getName());
    }

    @Test
    void updateCategory_withNonExistentId_shouldThrowException() {
        CategoryDto updateDto = new CategoryDto(999L, "Updated Category");

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.updateCategory(999L, updateDto);
        });
    }

    @Test
    void getCategories_shouldReturnPaginatedCategories() {
        // Создаем еще несколько категорий
        Category category2 = Category.builder().name("Category 2").build();
        Category category3 = Category.builder().name("Category 3").build();
        categoryRepository.saveAll(List.of(category2, category3));

        List<CategoryDto> result = categoryService.getCategories(0, 2);

        assertEquals(2, result.size());
    }

    @Test
    void getCategory_shouldReturnCategorySuccessfully() {
        CategoryDto result = categoryService.getCategory(existingCategory.getId());

        assertEquals(existingCategory.getId(), result.getId());
        assertEquals("Existing Category", result.getName());
    }

    @Test
    void getCategory_withNonExistentId_shouldThrowException() {
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategory(999L);
        });
    }

    @Test
    void getCategoryById_shouldReturnCategoryEntity() {
        Category result = categoryService.getCategoryById(existingCategory.getId());

        assertEquals(existingCategory.getId(), result.getId());
        assertEquals("Existing Category", result.getName());
    }

    @Test
    void getCategoryById_withNonExistentId_shouldThrowException() {
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategoryById(999L);
        });
    }
}