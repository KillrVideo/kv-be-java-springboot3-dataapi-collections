package com.killrvideo.controller;

import com.killrvideo.dao.RatingDao;
import com.killrvideo.dto.Rating;
import com.killrvideo.dto.RatingResponse;
import com.killrvideo.dto.RatingSummary;
import com.killrvideo.dto.RatingSummaryResponse;
import com.killrvideo.security.UserDetailsImpl;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/videos")
public class RatingsController {
    private static final Logger logger = LoggerFactory.getLogger(RatingsController.class);

    @Autowired
    private RatingDao ratingDao;

    /**
     * Submit a rating for a video
     */
    @PostMapping("/{videoId}/ratings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitRating(
            @PathVariable String videoId,
            @Valid @RequestBody RatingRequestBody body) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String userId = userDetails.getUserId();

        try {
            // Check if user has already rated this video
            Optional<Rating> existingRating = ratingDao.findByVideoIdAndUserId(videoId, userId);
            
            Rating ratingDTO;

            if (existingRating.isPresent()) {
                // Update existing rating
                ratingDTO = existingRating.get();
                ratingDTO.setRating(body.getRating().toString());
                ratingDao.update(ratingDTO);
                logger.info("Updated rating for video: {} by user: {}", videoId, userId);
            } else {
                // Create new rating
                ratingDTO = new Rating();
                ratingDTO.setVideoId(videoId);
                ratingDTO.setUserId(userId);
                ratingDTO.setRating(body.getRating().toString());
                ratingDao.save(ratingDTO);
                logger.info("Created new rating for video: {} by user: {}", videoId, userId);
            }

            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error submitting rating for video: {} by user: {}", videoId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting rating");
        }
    }

    @GetMapping("/{videoId}/ratings")
    public ResponseEntity<RatingResponse> getVideoRating(@PathVariable String videoId) {
        List<Rating> ratings = ratingDao.findByVideoId(videoId);
        RatingResponse response = new RatingResponse(ratings);
        //logger.debug("Retrieved ratings for video: {} - count: {}", videoId, ratings.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get rating summary for a video
     */
    @GetMapping("/id/{videoId}/rating")
    public ResponseEntity<RatingSummaryResponse> getAggregateVideoRating(@PathVariable String videoId) {

        try {
            List<Rating> ratings = ratingDao.findByVideoId(videoId);
            
            RatingSummary summary = new RatingSummary();
            summary.setVideoId(videoId);
            summary.setRatingCount(ratings.size());
            
            if (ratings.isEmpty()) {
                summary.setAverageRating("0.0");
            } else {
                float totalRating = ratings.stream()
                    .mapToInt(Rating::getRatingAsInt)
                    .sum();
                summary.setAverageRating(String.format("%.1f",(totalRating / ratings.size())));
            }

            logger.debug("Retrieved rating summary for video: {} - avg: {}, count: {}", 
                videoId, summary.getAverageRating(), summary.getRatingCount());
            
            RatingSummaryResponse response = new RatingSummaryResponse(summary);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting rating summary for video: {}", videoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{videoId}/ratings/user/{userId}")
    public ResponseEntity<RatingSummaryResponse> getUserRating(@PathVariable String videoId, @PathVariable String userId) {
        Optional<Rating> userRating = ratingDao.findByVideoIdAndUserId(videoId, userId);

        if (userRating.isPresent()) {
            RatingSummary summary = new RatingSummary();
            summary.setVideoId(videoId);
            summary.setRatingCount(1);
            summary.setAverageRating(String.format("%.1f", userRating.get().getRatingAsInt()));
            summary.setCurrentUserRating(userRating.get().getRatingAsInt());
            return ResponseEntity.ok(new RatingSummaryResponse(summary));

        } else {
            RatingSummary summary = new RatingSummary();
            summary.setVideoId(videoId);
            summary.setRatingCount(0);
            summary.setAverageRating("0.0");
            summary.setCurrentUserRating(0);
            return ResponseEntity.ok(new RatingSummaryResponse(summary));
        }
    }
} 

class RatingRequestBody {
    private Integer rating;

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}