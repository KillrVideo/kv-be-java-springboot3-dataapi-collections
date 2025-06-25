package com.killrvideo.dto;

public class UserResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String createdAt;

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
