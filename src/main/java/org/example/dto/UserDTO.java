package org.example.dto;

import lombok.Data;
import org.example.model.Discount;
import org.example.model.Role;
import org.example.model.User;

import java.util.List;
import java.util.UUID;

/**
ДТО для передачи информации о пользователе
и всех необходимых данных
для страницы пользователей на фронте
*/

@Data
public class UserDTO {

    // Пользователь
    private UUID id;
    private String username;
    private String password;
    private boolean active;
    private Role role;

    // Авторизованный пользователь
    private User userAuthentication;

    // Диапазон возможной скидки для пользователей
    private Discount discount;

    // Списки пользователей и ролей для отображения
    private List<Role> roleList;
    private List<User> userList;

    public UserDTO() {
    }

    public void setUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.active = user.isEnabled();
        this.role = user.getRole();
    }
}
