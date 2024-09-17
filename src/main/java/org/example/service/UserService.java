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

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired

    public List<User> findAllActive() {
        return userRepo.findAllActive();
    }

    public List<User> getAllUsersOperator() {
        return userRepo.getAllUsersOperator();
    }

    public User getUserById(UUID id) {
        return userRepo.findById(id).orElse(null);
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    public void updatePassword(String username, String password) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        saveUser(user);
    }

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

    public void deleteUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь c ID: " + userId + " не найден"));

        if (user != null) {

            user.setActive(false);
            saveUser(user);
        }
    }

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

    public User getAuthenticationUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User не найден"));
        return user;
    }

    public boolean isAdmin() {
        User user = getAuthenticationUser();
        return user.getRole().getName().equals("ADMIN");
    }
}
