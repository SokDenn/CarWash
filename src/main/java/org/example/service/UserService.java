package org.example.service;

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
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public User findByUsername(String username){
        return userRepo.findByUsername(username).orElse(null);
    }
    public void updatePassword (String username, String password){
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        saveUser(user);
    }

    public boolean createUser(String username, String password, UUID roleId) {
        User user = new User(username, passwordEncoder.encode(password));
        if (roleId != null) {
            user.setRole(roleRepo.findById(roleId).orElse(null));
        } else {
            user.setRole(roleRepo.findByName("USER").orElse(null));
        }

        saveUser(user);
        return true;
    }

    public boolean updateUser(UUID userId, String username, String password, UUID roleId) {

        User user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            if (roleId != null) {
                user.setRole(roleRepo.findById(roleId).orElse(null));
            }
            saveUser(user);
            return true;
        }
        return false;
    }

    public void deleteUser(UUID userId) {
        User user = userRepo.findById(userId).orElse(null);

        if (user != null) {

            user.setActive(false);
            saveUser(user);
        }
    }

    public boolean isUsernameUnique(UUID userId, String username) {
        User user = userRepo.findByUsername(username).orElse(null);

        if (user == null || user.getId().equals(userId)) {
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким логином не найден: " + username));

        } catch (UsernameNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return user;
    }

    public User getAuthenticationUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }
}
