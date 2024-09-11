package org.example.security;

import lombok.Data;

@Data
public class ActionResponse {
    private String url;
    private String message;

    public ActionResponse(){}

    public ActionResponse(String redirectUrl, String message) {
        this.url = redirectUrl;
        this.message = message;
    }
}
