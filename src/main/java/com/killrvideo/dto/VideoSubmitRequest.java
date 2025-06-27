package com.killrvideo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VideoSubmitRequest {
    @NotBlank(message = "YouTube URL is required")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$", 
             message = "Invalid YouTube URL format")
    private String youtubeUrl;

    private String description;

    private String[] tags;

    // Getters
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    // Setters
    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
} 