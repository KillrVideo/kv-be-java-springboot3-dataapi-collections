package com.killrvideo.dto;

public class RatingSummaryResponse {
    
    private RatingSummaryConversion data;
    private float averageRating;
    private float currentUserRating;

    public RatingSummaryResponse(RatingSummary data) {
        this.data = new RatingSummaryConversion(data);
        this.averageRating = data.getAverageRating();
    }

    public RatingSummaryConversion getData() {
        return data;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public float getCurrentUserRating() {
        return currentUserRating;
    }

    public void setCurrentUserRating(float currentUserRating) {
        this.currentUserRating = currentUserRating;
    }
}

class RatingSummaryConversion {
    private String videoId;
    private float averageRating;
    private int ratingCount;

    public RatingSummaryConversion(RatingSummary rating) {
        this.videoId = rating.getVideoId();
        this.averageRating = rating.getAverageRating();
        this.ratingCount = rating.getRatingCount();
    }

    public String getVideoId() {
        return videoId;
    }

    public float getAverageRating() {
        return averageRating;
    }
    
    public int getRatingCount() {
        return ratingCount;
    }
}