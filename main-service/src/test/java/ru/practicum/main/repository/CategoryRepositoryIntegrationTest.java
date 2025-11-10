package ru.practicum.main.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.main.model.Category;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByName_whenCategoryExists_shouldReturnCategory() {
        Category category = new Category();
        category.setName("Test Category");
        categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findByName("Test Category");

        assertTrue(found.isPresent());
        assertEquals("Test Category", found.get().getName());
    }

    @Test
    void existsByName_whenCategoryExists_shouldReturnTrue() {
        Category category = new Category();
        category.setName("Existing Category");
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByName("Existing Category");

        assertTrue(exists);
    }

    @Test
    void findAll_withPageable_shouldReturnPagedResults() {
        for (int i = 1; i <= 3; i++) {
            Category category = new Category();
            category.setName("Category " + i);
            categoryRepository.save(category);
        }

        Page<Category> page = categoryRepository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }
}