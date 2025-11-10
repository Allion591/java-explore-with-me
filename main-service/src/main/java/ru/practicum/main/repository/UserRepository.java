package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //Найти пользователей по списку ID с пагинацией
    @Query("SELECT u FROM User u WHERE (:ids IS NULL OR u.id IN :ids)")
    Page<User> findByIdIn(@Param("ids") List<Long> ids, Pageable pageable);

    //Проверить существование пользователя по email
    boolean existsByEmail(String email);

    //Найти пользователя по email
    Optional<User> findByEmail(String email);

    //Найти всех пользователей с пагинацией
    Page<User> findAll(Pageable pageable);
}