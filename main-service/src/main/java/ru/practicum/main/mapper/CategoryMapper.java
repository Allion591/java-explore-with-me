package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toCategory(NewCategoryDto newCategoryDto);
}