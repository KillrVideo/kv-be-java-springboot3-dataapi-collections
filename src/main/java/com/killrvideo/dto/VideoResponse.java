package com.killrvideo.dto;

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
//import java.time.Instant;
import java.util.Set;

public class VideoResponse {
    
    private String key;
    
    //@JsonProperty("video_id")
    private String videoId;
    
    //@JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("title")
    private String name;
    
    private String description;
    
    private Set<String> tags;
    
    private String location;
    
    @JsonProperty("thumbnailUrl")
    private String previewImageLocation;
    
    @JsonProperty("submittedAt")
    private Instant addedDate;

    @JsonProperty("uploadDate")
    private Instant uploadDate;

    // Additional metadata fields
    @JsonProperty("creator")
    private String userName;  // Combination of user's first and last name
    
    private long commentCount;

    private long views;
    
    //JsonProperty("processing_status")
    private String processingStatus;  // e.g., "PENDING", "COMPLETED", "FAILED"
    
    @JsonProperty("averageRating")
    private float rating;

    //private float[] vector;
    private DataAPIVector vector;

    //@JsonProperty("youtube_id")
    private String youtubeVideoId;

    // Getters

    public String getKey() {
        return key;
    }

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
    
    public Instant getUploadDate() {
        return uploadDate;
    }

    public String getUserName() {
        return userName;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long viewCount) {
        this.views = viewCount;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public float getRating() {
        return rating;
    }
    

    // Setters
    public void setKey(String key) {
        this.key = key;
    }

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

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = addedDate;
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

    public void setVector(DataAPIVector vector) {
        this.vector = vector;
    }

    public DataAPIVector getVector() {
        return vector;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    // Static factory method to create from Video
    public static VideoResponse fromVideo(Video video) {
        VideoResponse response = new VideoResponse();
        response.setKey(video.getVideoid());
        response.setVideoId(video.getVideoid());
        response.setUserId(video.getUserid());
        response.setName(video.getName());
        response.setDescription(video.getDescription());
        response.setTags(video.getTags());
        response.setLocation(video.getLocation());
        response.setPreviewImageLocation(video.getPreviewImageLocation());
        response.setAddedDate(video.getAddedDate());
        response.setUploadDate(video.getAddedDate());
        response.setVector(video.getVector());
        response.setYoutubeVideoId(video.getYoutubeId());
        response.setViews(video.getStats().getViews());
        // Additional fields will be set by the service layer
        // username, viewCount, commentCount, processingStatus
        return response;
    }
} 