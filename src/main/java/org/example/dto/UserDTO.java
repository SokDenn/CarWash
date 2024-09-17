package org.example.dto;

import lombok.Data;
import org.example.model.Discount;
import org.example.model.Role;
import org.example.model.User;

import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {

    private UUID id;
    private String username;
    private String password;
    private boolean active;
    private Role role;

    private User userAuthentication;
    private Discount discount;

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
