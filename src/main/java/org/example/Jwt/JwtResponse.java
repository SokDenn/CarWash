package org.example.Jwt;

import lombok.Data;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;

    public JwtResponse() {
    }

    public JwtResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
