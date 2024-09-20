package org.example.repo;

import org.example.model.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;
/**
 * Репозиторий для работы с сущностью Role.
 */

public interface RoleRepo extends CrudRepository<Role, UUID> {

    /**
     * Найти роль по её уникальному идентификатору (UUID).
     *
     * @param roleId идентификатор роли
     * @return Optional с найденной ролью или пустой Optional, если роль не найдена
     */
    Optional<Role> findById(UUID roleId);

    /**
     * Найти роль по её имени.
     *
     * @param name имя роли
     * @return Optional с найденной ролью или пустой Optional, если роль не найдена
     */
    Optional<Role> findByName(String name);

}
