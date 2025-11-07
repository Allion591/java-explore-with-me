package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.enums.ParticipationRequestStatus;
import ru.practicum.main.model.ParticipationRequest;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    //Найти заявки по ID пользователя
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    //Найти заявки по ID события
    List<ParticipationRequest> findByEventId(Long eventId);

    //Найти заявки по ID события и статусу
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    //Найти заявку по ID пользователя и ID события
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    //Подсчитать количество подтвержденных заявок для события
    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    //Подсчитать количество подтвержденных заявок для списка событий
    @Query("SELECT pr.event.id, COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.event.id IN :eventIds AND pr.status = 'CONFIRMED' " +
            "GROUP BY pr.event.id")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    //Найти заявки по ID события и списку ID заявок
    List<ParticipationRequest> findByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    //Проверить существование заявки от пользователя на событие
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    //Найти все заявки по списку ID
    List<ParticipationRequest> findByIdIn(List<Long> requestIds);
}