package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecommendationResponse {
    private VideoResponse video;
    
    @JsonProperty("similarity_score")
    private double similarityScore;

    // Getters
    public VideoResponse getVideo() {
        return video;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    // Setters
    public void setVideo(VideoResponse video) {
        this.video = video;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
} 