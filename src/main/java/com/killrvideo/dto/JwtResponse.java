package com.killrvideo.dto;

//import lombok.Data;

//@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String userId;
    private String email;

    public JwtResponse(String accessToken, String userId, String email) {
        this.token = accessToken;
        this.userId = userId;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }
    
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
} 