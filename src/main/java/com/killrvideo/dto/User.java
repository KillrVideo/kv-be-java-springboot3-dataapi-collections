package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public class User {
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("firstname")
    private String firstName;
    
    @JsonProperty("lastname")
    private String lastName;
    
    private String email;
    
    @JsonProperty("hashed_password")
    private String hashedPassword;
    
    @JsonProperty("created_at")
    private String createdAt;

    private String roles;

    public User(String userId, String firstName, String lastName, String email, String hashedPassword, String createdAt, String roles) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getRoles() {
        return roles;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
} 