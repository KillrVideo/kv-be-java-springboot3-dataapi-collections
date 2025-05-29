package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class DeleteCommentRequest {
    @NotBlank(message = "Comment ID is required")
    @JsonProperty("comment_id")
    private String commentId;

    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}