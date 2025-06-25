package com.killrvideo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubmitCommentRequest {
    @NotBlank(message = "Comment text is required")
    @JsonProperty("text")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    private String commentText;

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
} 