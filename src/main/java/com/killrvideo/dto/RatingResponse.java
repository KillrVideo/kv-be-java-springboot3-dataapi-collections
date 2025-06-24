package com.killrvideo.dto;

import java.util.ArrayList;
import java.util.List;

public class RatingResponse {
    
    private List<RatingConversion> data;
    private float averageRating;

    public RatingResponse(List<Rating> ratings) {
        List<RatingConversion> dataResponse = new ArrayList<>();
        float totalRating = 0;
        for (Rating rating : ratings) {
            dataResponse.add(new RatingConversion(rating));
            totalRating += Float.parseFloat(rating.getRating());
        }

        this.averageRating = totalRating / ratings.size();
        this.data = dataResponse;
    }

    public List<RatingConversion> getData() {
        return data;
    }

    public float getAverageRating() {
        return averageRating;
    }
}

class RatingConversion {
    private String videoId;
    private float averageRating;
    private int ratingCount;

    public RatingConversion(Rating rating) {
        this.videoId = rating.getVideoId();
        this.averageRating = Float.parseFloat(rating.getRating());
        this.ratingCount = 1;
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
