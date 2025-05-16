package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Set;

public class VideoResponse {
    @JsonProperty("video_id")
    private String videoId;
    
    @JsonProperty("user_id")
    private String userId;
    
    private String name;
    
    private String description;
    
    private Set<String> tags;
    
    private String location;
    
    @JsonProperty("preview_image_location")
    private String previewImageLocation;
    
    @JsonProperty("added_date")
    private Instant addedDate;
    
    // Additional metadata fields
    @JsonProperty("user_name")
    private String userName;  // Combination of user's first and last name
    
    @JsonProperty("comment_count")
    private long commentCount;
    
    @JsonProperty("processing_status")
    private String processingStatus;  // e.g., "PENDING", "COMPLETED", "FAILED"
    
    // Getters
    public String getVideoId() {
        return videoId;
    }

    public String getUserId() {
        return userId;
    }

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

    public String getPreviewImageLocation() {
        return previewImageLocation;
    }

    public Instant getAddedDate() {
        return addedDate;
    }

    public String getUserName() {
        return userName;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    // Setters
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public void setPreviewImageLocation(String previewImageLocation) {
        this.previewImageLocation = previewImageLocation;
    }

    public void setAddedDate(Instant addedDate) {
        this.addedDate = addedDate;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    // Static factory method to create from Video
    public static VideoResponse fromVideo(Video video) {
        VideoResponse response = new VideoResponse();
        response.setVideoId(video.getVideoId());
        response.setUserId(video.getUserId());
        response.setName(video.getName());
        response.setDescription(video.getDescription());
        response.setTags(video.getTags());
        response.setLocation(video.getLocation());
        response.setPreviewImageLocation(video.getPreviewImageLocation());
        response.setAddedDate(video.getAddedDate());
        // Additional fields will be set by the service layer
        return response;
    }
} 