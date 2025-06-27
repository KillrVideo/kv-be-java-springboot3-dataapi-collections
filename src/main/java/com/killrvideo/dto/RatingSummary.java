package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingSummary {
    @JsonProperty("video_id")
    private String videoId;
    
    @JsonProperty("average_rating")
    private String averageRating;
    
    @JsonProperty("rating_count")
    private int ratingCount;

    private int currentUserRating;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getCurrentUserRating() {
        return currentUserRating;
    }

    public void setCurrentUserRating(int currentUserRating) {
        this.currentUserRating = currentUserRating;
    }
} 