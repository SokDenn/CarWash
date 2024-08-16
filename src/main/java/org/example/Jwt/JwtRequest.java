package org.example.Jwt;

import lombok.Data;

@Data
public class JwtRequest {
    private String username;
    private String password;
    private String token;

    public JwtRequest() {
    }
}
