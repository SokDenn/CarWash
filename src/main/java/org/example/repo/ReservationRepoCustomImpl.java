package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.converter.StringConverter;
import org.example.model.Reservation;
import org.example.model.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация кастомного репозитория ReservationRepoCustom.
 * Реализует логику для кастомных запросов бронирований с динамическими критериями.
 */

public class ReservationRepoCustomImpl implements ReservationRepoCustom {
    @Autowired
    UserRepo userRepo;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private StringConverter stringConverter;
    @Autowired
    private UserService userService;

    /**
     * Найти бронирования для конкретного бокса, соответствующие заданным параметрам:
     * Если пользователь аутентифицирован как USER, то возвращаются только его бронирования.
     *
     * @param boxId идентификатор бокса
     * @param startDataTime начало временного диапазона
     * @param endDateTime конец временного диапазона
     * @param statusList список статусов бронирований
     * @return список бронирований, удовлетворяющих условиям
     */
    @Override
    public List<Reservation> findReservations(UUID boxId, LocalDateTime startDataTime,
                                              LocalDateTime endDateTime, List<String> statusList) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reservation> query = cb.createQuery(Reservation.class);
        Root<Reservation> reservation = query.from(Reservation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (!ObjectUtils.isEmpty(boxId)) {
            predicates.add(cb.equal(reservation.get("box").get("id"), boxId));
        }

        if (!ObjectUtils.isEmpty(startDataTime)) {
            predicates.add(cb.greaterThanOrEqualTo(reservation.get("startDateTime"), startDataTime));
        }

        if (!ObjectUtils.isEmpty(endDateTime)) {
            predicates.add(cb.lessThanOrEqualTo(reservation.get("endDateTime"), endDateTime));
        }

        if (!ObjectUtils.isEmpty(statusList)) {
            predicates.add(reservation.get("status").in(statusList));
        }

        User authenticatedUser = userService.getAuthenticationUser();
        if (authenticatedUser.getRole().getName().equals("USER")) {
            predicates.add(cb.equal(reservation.get("user"), authenticatedUser));
        }

        // Условие для исключения удалённых записей
        predicates.add(cb.isFalse(reservation.get("isDeleted")));

        // Применение условий
        query.where(predicates.toArray(new Predicate[0]));

        // Сортировка по времени
        query.orderBy(cb.desc(reservation.get("startDateTime")));

        return entityManager.createQuery(query).getResultList();

    }
}
