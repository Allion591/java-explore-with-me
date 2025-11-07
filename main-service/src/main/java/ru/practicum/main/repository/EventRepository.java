package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.dto.filter.AdminEventFilterParams;
import ru.practicum.main.dto.filter.EventPublicFilterRequest;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.model.Event;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    //Найти события по инициатору с пагинацией
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    //Найти событие по ID и инициатору
    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    //Найти события по списку ID
    List<Event> findByIdIn(List<Long> ids);

    //Админский поиск событий с фильтрами
    @Query("SELECT e, " +
            "(SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event = e AND pr.status = 'CONFIRMED') as confirmedRequests " +
            "FROM Event e WHERE " +
            "(COALESCE(:#{#filter.users}, NULL) IS NULL OR e.initiator.id IN :#{#filter.users}) AND " +
            "(COALESCE(:#{#filter.states}, NULL) IS NULL OR e.state IN :#{#filter.states}) AND " +
            "(COALESCE(:#{#filter.categories}, NULL) IS NULL OR e.category.id IN :#{#filter.categories}) AND " +
            "(COALESCE(:#{#filter.rangeStart}, NULL) IS NULL OR e.eventDate >= :#{#filter.rangeStart}) AND " +
            "(COALESCE(:#{#filter.rangeEnd}, NULL) IS NULL OR e.eventDate <= :#{#filter.rangeEnd})")
    Page<Object[]> findEventsByAdminWithCounts(@Param("filter") AdminEventFilterParams filter, Pageable pageable);

    //Публичный поиск событий с фильтрами
    @Query("SELECT e FROM Event e WHERE " +
            "e.state = 'PUBLISHED' AND " +
            "(COALESCE(:#{#filter.text}, '') = '' OR " +
            "LOWER(e.annotation) LIKE LOWER(CONCAT('%', :#{#filter.text}, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :#{#filter.text}, '%'))) AND " +
            "(COALESCE(:#{#filter.categories}, NULL) IS NULL OR e.category.id IN :#{#filter.categories}) AND " +
            "(COALESCE(:#{#filter.paid}, NULL) IS NULL OR e.paid = :#{#filter.paid}) AND " +
            "(COALESCE(:#{#filter.rangeStart}, NULL) IS NULL OR e.eventDate >= :#{#filter.rangeStart}) AND " +
            "(COALESCE(:#{#filter.rangeEnd}, NULL) IS NULL OR e.eventDate <= :#{#filter.rangeEnd}) AND " +
            "(COALESCE(:#{#filter.onlyAvailable}, false) = false OR " +
            "e.participantLimit = 0 OR " +
            "e.participantLimit > (SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event = e AND r.status = 'CONFIRMED'))")
    Page<Event> findEventsByPublic(@Param("filter") EventPublicFilterRequest filter, Pageable pageable);

    //Найти опубликованные события по ID
    Optional<Event> findByIdAndState(Long id, EventState state);

    //Найти события по категории
    List<Event> findByCategoryId(Long categoryId);

    //Проверить существование события по ID и инициатору
    boolean existsByIdAndInitiatorId(Long id, Long initiatorId);

    //Найти события для подборок
    @Query("SELECT e FROM Event e WHERE e.id IN :eventIds AND e.state = 'PUBLISHED'")
    List<Event> findPublishedEventsByIds(@Param("eventIds") List<Long> eventIds);
}