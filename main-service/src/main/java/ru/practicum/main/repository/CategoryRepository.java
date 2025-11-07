package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Category;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    //Найти категорию по имени (для проверки уникальности)
    Optional<Category> findByName(String name);

    //Проверить существование категории по имени
    boolean existsByName(String name);

    //Найти все категории с пагинацией
    Page<Category> findAll(Pageable pageable);

    //Проверить, используется ли категория в каких-либо событиях
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean isCategoryUsed(@Param("categoryId") Long categoryId);
}