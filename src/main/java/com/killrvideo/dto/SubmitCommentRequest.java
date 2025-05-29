package com.killrvideo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubmitCommentRequest {
    @NotBlank(message = "Video ID is required")
    @JsonProperty("video_id")
    private String videoId;

    @NotBlank(message = "Comment text is required")
    @JsonProperty("comment_text")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    private String commentText;

    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    // Getters
    public String getVideoId() {
        return videoId;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getUserId() {
        return userId;
    }

    // Setters
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 