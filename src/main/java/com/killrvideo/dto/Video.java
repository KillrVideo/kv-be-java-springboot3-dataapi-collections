package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Set;

public class Video {
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
    
    @JsonProperty("$vector")
    private float[] vector;
    
    @JsonProperty("added_date")
    private Instant addedDate;
    
    private boolean deleted;
    
    @JsonProperty("deleted_at")
    private Instant deletedAt;

    private String processingStatus = "PENDING";
    private long views;

    @JsonProperty("youtube_id")
    private String youtubeId;

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

    public float[] getVector() {
        return vector;
    }

    public Instant getAddedDate() {
        return addedDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public long getViews() {
        return views;
    }

    public String getYoutubeId() {
        return youtubeId;
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

    public void setVector(float[] videoVector) {
        this.vector = videoVector;
    }

    public void setAddedDate(Instant addedDate) {
        this.addedDate = addedDate;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }
} 