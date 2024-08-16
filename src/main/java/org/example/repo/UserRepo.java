package org.example.repo;

import org.example.model.Box;
import org.example.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends CrudRepository<User, UUID> {
    Optional<User> findById(UUID userId);
    @Query("SELECT u FROM User u WHERE u.active = true and u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.role.name like 'OPERATOR' and u.active = true order by u.username")
    List<User> getAllUsersOperator();

    @Query("SELECT u FROM User u WHERE u.active = true order by u.username")
    List<User> findAllActive();
}

