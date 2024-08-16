package org.example.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.converter.StringConverter;
import org.example.model.Reservation;
import org.example.model.Status;
import org.example.model.User;
import org.example.repo.ReservationRepoCastom;
import org.example.repo.UserRepo;
import org.example.service.BoxService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservationRepoCastomImpl implements ReservationRepoCastom {
    @Autowired
    UserRepo userRepo;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private StringConverter stringConverter;
    @Autowired
    private UserService userService;

    @Override
    public List<Reservation> findReservations(UUID boxId, String startDataTimeStr,
                                              String endDateTimeStr, List<String> statusList) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reservation> query = cb.createQuery(Reservation.class);
        Root<Reservation> reservation = query.from(Reservation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (boxId != null) {
            predicates.add(cb.equal(reservation.get("box").get("id"), boxId));
        }

        if (startDataTimeStr != null && startDataTimeStr != "") {
            LocalDateTime startDataTime = stringConverter.convertDataTime(startDataTimeStr);
            predicates.add(cb.greaterThanOrEqualTo(reservation.get("startDateTime"), startDataTime));
        }

        if (endDateTimeStr != null && endDateTimeStr != "") {
            LocalDateTime endDateTime = stringConverter.convertDataTime(endDateTimeStr);
            predicates.add(cb.lessThanOrEqualTo(reservation.get("endDateTime"), endDateTime));
        }

        if (!statusList.isEmpty()) {
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
