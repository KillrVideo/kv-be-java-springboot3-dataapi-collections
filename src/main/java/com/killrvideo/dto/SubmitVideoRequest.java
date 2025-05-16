package com.killrvideo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class SubmitVideoRequest {
    @NotBlank(message = "Video name is required")
    @Size(min = 3, max = 100, message = "Video name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Set<String> tags;

    @NotBlank(message = "Video location/URL is required")
    private String location;

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getLocation() {
        return location;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setLocation(String location) {
        this.location = location;
    }
} 