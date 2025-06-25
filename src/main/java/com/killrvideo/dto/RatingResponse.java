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
            RatingConversion localRating = new RatingConversion(rating);
            dataResponse.add(localRating);
            totalRating += localRating.getAverageRating();
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
        this.averageRating = parseRating(rating.getRating());
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

    private float parseRating(String rating) {
        try {
            return Float.parseFloat(rating);
        } catch (NumberFormatException e) {
            // parse rating from string
            StringBuilder ratingBuilder = new StringBuilder();
            for (int index = 0; index < rating.length(); index++) {
                char c = rating.charAt(index);
                if (Character.isDigit(c) || c == '.') {
                    ratingBuilder.append(c);
                } else if (ratingBuilder.length() > 0) {
                    // if we have already parsed a digit, and then get a non-digit, break
                    break;
                }
            }

            if (ratingBuilder.length() > 0) {
                return Float.parseFloat(ratingBuilder.toString());
            }

            return 0.0f;
        }
    }
}
