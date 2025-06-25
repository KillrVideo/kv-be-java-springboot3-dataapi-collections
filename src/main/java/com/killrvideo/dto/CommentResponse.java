package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class CommentResponse {
    @JsonProperty("commentid")
    private String commentId;
    
    @JsonProperty("videoid")
    private String videoId;
    
    @JsonProperty("userid")
    private String userId;

    private String comment;
    
    private Instant timestamp;
    
    @JsonProperty("sentiment_score")
    private float sentimentScore;

    private String firstName;

    private String lastName;

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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public float getSentimentScore() {
        return sentimentScore;
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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSentimentScore(float sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    // Static factory method to create from Comment
    public static CommentResponse fromComment(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setVideoId(comment.getVideoId());
        response.setUserId(comment.getUserId());
        response.setComment(comment.getComment());
        response.setTimestamp(comment.getTimestamp());

        if (comment.getUserName() == null) {
            String userId = comment.getUserId();
            int firstDashIndex = userId.indexOf('-');
            comment.setUserName(userId.substring(0, firstDashIndex));
        }
        response.setUserName(comment.getUserName());
        // Additional fields will be set by the service layer
        return response;
    }
} 