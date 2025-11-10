package ru.practicum.main.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.exception.conflict.CategoryNotEmptyException;
import ru.practicum.main.exception.notFound.CategoryNotFoundException;
import ru.practicum.main.exception.validation.CategoryNameConflictException;
import ru.practicum.main.mapper.CategoryMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.service.interfaces.CategoryService;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание категории: {}", newCategoryDto.getName());

        try {
            Category category = categoryMapper.toCategory(newCategoryDto);
            Category savedCategory = categoryRepository.save(category);
            log.info("Категории присвоен id: {}", savedCategory.getId());
            return categoryMapper.toCategoryDto(savedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Категория с именем '{}' уже существует", newCategoryDto.getName());
            throw new CategoryNameConflictException(newCategoryDto.getName());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Удаление категории с id: {}", categoryId);

        Category category = getCategoryById(categoryId);

        // Проверка, что категория не используется в событиях
        if (categoryRepository.isCategoryUsed(categoryId)) {
            log.error("Категория уже используется в событиях");
            throw new CategoryNotEmptyException(categoryId);
        }

        categoryRepository.delete(category);
        log.info("Категория с id {} удалена", categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("Обновление категории с id: {}", categoryId);

        Category category = getCategoryById(categoryId);

        // Проверка уникальности имени, если оно изменилось
        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            log.error("Данное имя категории уже используется");
            throw new CategoryNameConflictException(categoryDto.getName());
        }

        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);

        log.info("Категория с id {} обновлена", categoryId);
        return categoryMapper.toCategoryDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Получение категории from: {}, size: {}", from, size);

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        return categoryRepository.findAll(pageable).getContent().stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        log.info("Получение категории с id: {}", categoryId);

        Category category = getCategoryById(categoryId);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}