package com.killrvideo.dto;

import jakarta.validation.constraints.Size;
import java.util.Set;

public class VideoUpdateRequest {
    @Size(min = 3, max = 100, message = "Video name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Set<String> tags;

    // Note: We don't allow updating the location after upload
    // This helps maintain data integrity and prevents abuse

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
} 