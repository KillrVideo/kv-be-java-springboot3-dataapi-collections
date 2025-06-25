package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class Comment {
    @JsonProperty("comment_id")
    private String commentId;
    
    @JsonProperty("video_id")
    private String videoId;
    
    @JsonProperty("user_id")
    private String userId;
    
    private String comment;
    
    private Instant timestamp;

    // Additional metadata fields
    @JsonProperty("user_name")
    private String userName;  // Combination of user's first and last name

    // Getters
    public String getCommentId() {
        return commentId;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getUserId() {
        return userId;
    }

    public String getComment() {
        return comment;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getUserName() {
        return userName;
    }

    // Setters
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
} 