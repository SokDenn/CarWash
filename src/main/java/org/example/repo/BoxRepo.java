package org.example.repo;

import org.example.model.Box;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Box.
 */
public interface BoxRepo extends CrudRepository<Box, UUID> {
    /**
     * Найти Box по её UUID.
     *
     * @param boxId уникальный идентификатор бокса
     * @return Optional с найденным боксом или пустой объект Optional
     */
    Optional<Box> findById(UUID boxId);

    /**
     * Получить все уникальные номера боксов, которые не удалены.
     *
     * @return отсортированный набор уникальных номеров боксов
     */
    @Query("SELECT DISTINCT b.boxNumber FROM Box b WHERE b.isDeleted = false")
    SortedSet<Integer> findAllBoxNumber();

    /**
     * Найти все активные (не удаленные) боксы и отсортировать их по номеру.
     *
     * @return список всех активных боксов, отсортированных по номеру
     */
    @Query("SELECT b FROM Box b WHERE b.isDeleted = false order by b.boxNumber")
    List<Box> findAllActive();
}
