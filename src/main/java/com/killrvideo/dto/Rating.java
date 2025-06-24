package com.killrvideo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class Rating {
    @JsonProperty("_id")
    private String ratingId;
    
    @JsonProperty("video_id")
    private String videoId;
    
    @JsonProperty("user_id")
    private String userId;
    
    private String rating; // 1-5 rating
    
    // Getters
    public String getRatingId() {
        return ratingId;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRating() {
        return rating;
    }

    public Integer getRatingAsInt() {
        try {
            return Integer.parseInt(rating);
        } catch (NumberFormatException e) {
            StringBuilder numbersString = new StringBuilder();

            try {
                for (int index = 0; index < rating.length(); index++) {
                    if (Character.isDigit(rating.charAt(index))) {
                        numbersString.append(rating.charAt(index));
                    } else {
                        if (numbersString.length() > 0) {
                            break;
                        }
                    }
                }

                return Integer.parseInt(numbersString.toString());
            } catch (NumberFormatException e2) {
                System.out.println("Error parsing rating: " + rating);
                return 0;
            }
        }
    }

    // Setters
    public void setRatingId(String ratingId) {
        this.ratingId = ratingId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
} 