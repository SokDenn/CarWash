package org.example.Jwt;

import lombok.Data;

/**
 * Класс для обработки запросов с JWT.
 */
@Data
public class JwtRequest {
    private String username;
    private String password;
    private String token;

    public JwtRequest() {
    }
}
