package org.example.repo;

import org.example.model.Washing;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Washing.
 */
public interface WashingRepo extends CrudRepository<Washing, UUID> {

    /**
     * Найти мойку по её уникальному идентификатору (UUID).
     *
     * @param userId идентификатор мойки
     * @return Optional с найденной мойкой или пустой Optional, если мойка не найдена
     */
    Optional<Washing> findById(UUID userId);

    /**
     * Получить список всех активных моек (не удалённых), отсортированных по имени.
     *
     * @return список активных моек
     */
    @Query("SELECT w FROM Washing w WHERE w.isDeleted = false ORDER BY w.name")
    List<Washing> findAllActive();
}
