package org.example.repo;

import org.example.model.Box;
import org.example.model.Status;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

public interface BoxRepo extends CrudRepository<Box, UUID> {
    Optional<Box> findById(UUID boxId);
    @Query("SELECT DISTINCT b.boxNumber FROM Box b WHERE b.isDeleted = false")
    SortedSet<Integer> findAllBoxNumber();

    @Query("SELECT b FROM Box b WHERE b.isDeleted = false order by b.boxNumber")
    List<Box> findAllActive();
}
