package com.killrvideo.dao;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filters;

import com.killrvideo.dto.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RatingDao {
    private static final Logger logger = LoggerFactory.getLogger(RatingDao.class);
    private final Collection<Rating> ratingCollection;

    @Autowired
    public RatingDao(Database killrVideoDatabase) {
        this.ratingCollection = killrVideoDatabase.getCollection("ratings", Rating.class);
        logger.info("Initialized RatingDao with 'ratings' collection");
    }

    /**
     * Saves a new rating to the database.
     * If the ratingId is not set, generates a new UUID.
     *
     * @param rating The rating to save
     * @return The saved rating
     */
    public Rating save(Rating rating) {
        if (rating.getRatingId() == null) {
            rating.setRatingId(UUID.randomUUID().toString());
            logger.debug("Generated new rating ID: {}", rating.getRatingId());
        }
        ratingCollection.insertOne(rating);
        logger.debug("Saved rating with ID: {}", rating.getRatingId());
        return rating;
    }

    /**
     * Finds a rating by video ID and user ID.
     *
     * @param videoId The ID of the video
     * @param userId The ID of the user
     * @return Optional containing the rating if found, empty otherwise
     */
    public Optional<Rating> findByVideoIdAndUserId(String videoId, String userId) {
        logger.debug("Finding rating for video: {} and user: {}", videoId, userId);
        return ratingCollection.findOne(
            Filters.and(
                Filters.eq("video_id", videoId),
                Filters.eq("user_id", userId)
            )
        );
    }

    /**
     * Finds all ratings for a specific video.
     *
     * @param videoId The ID of the video
     * @return FindIterable of ratings
     */
    public List<Rating> findByVideoId(String videoId) {
        logger.debug("Finding all ratings for video: {}", videoId);
        return ratingCollection.find(
            Filters.eq("video_id", videoId)).toList();
    }

    /**
     * Updates an existing rating.
     *
     * @param rating The rating to update
     * @throws IllegalArgumentException if rating ID is null
     */
    public void update(Rating rating) {
        logger.debug("Updating rating with ID: {}", rating.getRatingId());
        try {
            ratingCollection.replaceOne(Filters.eq("rating_id", rating.getRatingId()), rating);
        } catch (Exception e) {
            logger.error("Error updating rating: {} {}", rating, e.getMessage());
            throw new IllegalArgumentException("Error updating rating: " + e.getMessage());
        }
    }

    /**
     * Deletes a rating by its ID.
     *
     * @param ratingId The ID of the rating to delete
     */
    public void deleteById(String ratingId) {
        logger.debug("Deleting rating with ID: {}", ratingId);
        ratingCollection.deleteOne(Filters.eq("rating_id", ratingId));
    }
} 