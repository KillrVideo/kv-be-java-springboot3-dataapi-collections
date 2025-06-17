package com.killrvideo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class VideoSubmitRequest {
    @NotBlank(message = "YouTube URL is required")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$", 
             message = "Invalid YouTube URL format")
    private String youtubeUrl;

    @Size(min = 1, max = 150, message = "Title must be between 1 and 150 characters")
    private String title;

    // Getters
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public String getTitle() {
        return title;
    }

    // Setters
    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }
} 