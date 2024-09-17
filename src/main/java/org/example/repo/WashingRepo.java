package org.example.repo;

import org.example.model.Washing;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WashingRepo extends CrudRepository<Washing, UUID> {
    Optional<Washing> findById(UUID userId);

    @Query("SELECT w FROM Washing w WHERE w.isDeleted = false ORDER BY w.name")
    List<Washing> findAllActive();
}
