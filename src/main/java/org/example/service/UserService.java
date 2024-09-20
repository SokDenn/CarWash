package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.model.User;
import org.example.repo.RoleRepo;
import org.example.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления пользователями и аутентификацией.
 */

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Получить всех активных пользователей.
     *
     * @return список активных пользователей
     */
    public List<User> findAllActive() {
        return userRepo.findAllActive();
    }

    /**
     * Получить всех пользователей с ролью оператора.
     *
     * @return список пользователей-операторов
     */
    public List<User> getAllUsersOperator() {
        return userRepo.getAllUsersOperator();
    }

    /**
     * Получить пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь или null, если не найден
     */
    public User getUserById(UUID id) {
        return userRepo.findById(id).orElse(null);
    }

    /**
     * Сохранить пользователя.
     *
     * @param user пользователь для сохранения
     * @return сохраненный пользователь
     */
    public User saveUser(User user) {
        return userRepo.save(user);
    }

    /**
     * Найти пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return пользователь или null, если не найден
     */
    public User findByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    /**
     * Обновить пароль пользователя.
     *
     * @param username имя пользователя
     * @param password новый пароль
     */
    public void updatePassword(String username, String password) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        saveUser(user);
    }

    /**
     * Создать нового пользователя.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @param roleId идентификатор роли
     * @return true, если пользователь успешно создан
     */
    public boolean createUser(String username, String password, UUID roleId) {
        User user = new User(username, passwordEncoder.encode(password));
        if (roleId != null) {
            user.setRole(roleRepo.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("Роль c ID: " + roleId + " не найдена")));

        } else {
            user.setRole(roleRepo.findByName("USER").orElse(null));
        }

        saveUser(user);
        return true;
    }


    /**
     * Обновить данные пользователя.
     *
     * @param userId идентификатор пользователя
     * @param username новое имя пользователя
     * @param password новый пароль
     * @param roleId идентификатор новой роли
     * @return true, если пользователь успешно обновлен
     */
    public boolean updateUser(UUID userId, String username, String password, UUID roleId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь c ID: " + userId + " не найден"));

        if (user != null) {
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            if (roleId != null) {
                user.setRole(roleRepo.findById(roleId)
                        .orElseThrow(() -> new EntityNotFoundException("Роль c ID: " + roleId + " не найдена")));
            }
            saveUser(user);
            return true;
        }
        return false;
    }

    /**
     * Удалить пользователя по идентификатору, отметив его как неактивного.
     *
     * @param userId идентификатор пользователя
     */
    public void deleteUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь c ID: " + userId + " не найден"));

        if (user != null) {

            user.setActive(false);
            saveUser(user);
        }
    }

    /**
     * Проверить, уникально ли имя пользователя.
     *
     * @param userId идентификатор пользователя
     * @param username имя пользователя
     * @return true, если имя пользователя уникально
     */
    public boolean isUsernameUnique(UUID userId, String username) {
        Optional<User> optionalUser = userRepo.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return true;
        }

        User user = optionalUser.get();
        if (user.getId().equals(userId)) {
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким логином не найден: " + username));
    }

    /**
     * Получить текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь
     */
    public User getAuthenticationUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User не найден"));
        return user;
    }

    /**
     * Проверить, является ли текущий пользователь администратором.
     *
     * @return true, если текущий пользователь администратор
     */
    public boolean isAdmin() {
        User user = getAuthenticationUser();
        return user.getRole().getName().equals("ADMIN");
    }
}
