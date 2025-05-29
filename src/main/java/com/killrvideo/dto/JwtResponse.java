package com.killrvideo.dto;

//import lombok.Data;

//@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String email;

    public JwtResponse(String accessToken, String id, String email) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }
    
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
} 