package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Compilation;
import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    //Найти подборки по признаку закрепления с пагинацией
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    //Найти все подборки с пагинацией
    Page<Compilation> findAll(Pageable pageable);

    //Проверить существование подборки по заголовку
    boolean existsByTitle(String title);

    //Найти подборку по заголовку
    Optional<Compilation> findByTitle(String title);

    //Найти подборки, содержащие определенное событие
    @Query("SELECT c FROM Compilation c JOIN c.events e WHERE e.id = :eventId")
    List<Compilation> findByEventId(@Param("eventId") Long eventId);
}