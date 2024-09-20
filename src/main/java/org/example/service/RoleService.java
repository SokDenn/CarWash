package org.example.service;

import org.example.model.Role;
import org.example.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для управления ролями пользователей.
 */
@Service
public class RoleService {
    @Autowired
    RoleRepo roleRepo;

    /**
     * Получить все роли.
     *
     * @return список всех ролей
     */
    public List<Role> findAll() {
        return (List<Role>) roleRepo.findAll();
    }
}
