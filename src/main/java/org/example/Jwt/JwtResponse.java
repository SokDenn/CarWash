package org.example.Jwt;

import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private ResponseCookie accessTokenCookie;

    public JwtResponse() {
    }

    public JwtResponse(String accessToken, String refreshToken, ResponseCookie accessTokenCookie) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenCookie = accessTokenCookie;
    }
}
