package org.example.repo;

import org.example.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью User.
 */
public interface UserRepo extends CrudRepository<User, UUID> {
    /**
     * Найти пользователя по его уникальному идентификатору (UUID).
     *
     * @param userId идентификатор пользователя
     * @return Optional с найденным пользователем или пустой Optional, если пользователь не найден
     */
    Optional<User> findById(UUID userId);

    /**
     * Найти активного пользователя по его имени пользователя (username).
     *
     * @param username имя пользователя
     * @return Optional с найденным активным пользователем или пустой Optional, если пользователь не найден
     */
    @Query("SELECT u FROM User u WHERE u.active = true and u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Получить список всех активных пользователей с ролью "OPERATOR", отсортированных по имени пользователя.
     *
     * @return список активных пользователей с ролью "OPERATOR"
     */
    @Query("SELECT u FROM User u WHERE u.role.name like 'OPERATOR' and u.active = true order by u.username")
    List<User> getAllUsersOperator();

    /**
     * Получить список всех активных пользователей, отсортированных по имени пользователя.
     *
     * @return список всех активных пользователей
     */
    @Query("SELECT u FROM User u WHERE u.active = true order by u.username")
    List<User> findAllActive();
}

