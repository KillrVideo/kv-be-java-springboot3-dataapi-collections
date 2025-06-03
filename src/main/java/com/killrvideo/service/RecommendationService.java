package com.killrvideo.service;

import com.killrvideo.dao.VideoDao;
import com.killrvideo.dto.Video;
import com.killrvideo.dto.VideoResponse;
import com.killrvideo.dto.RecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private VideoDao videoDao;

    /**
     * Get similar video recommendations based on vector similarity.
     * 
     * @param videoId The ID of the source video
     * @param limit Maximum number of recommendations to return
     * @return List of recommendations with similarity scores
     */
    public List<RecommendationResponse> getSimilarVideos(String videoId, int limit) {
        logger.debug("Finding similar videos for videoId: {}, limit: {}", videoId, limit);

        // Get the source video with its vector
        Optional<Video> sourceVideoOpt = videoDao.findByVideoId(videoId, true);

        if (sourceVideoOpt.isEmpty()) {
            logger.warn("Source video not found: {}", videoId);
            throw new IllegalArgumentException("Video not found");
        }

        Video sourceVideo = sourceVideoOpt.get();
        float[] sourceVector = sourceVideo.getVector();

        if (sourceVector == null) {
            logger.warn("Source video has no vector: {}", videoId);
            throw new IllegalStateException("Video has no vector embedding");
        }

        // Find similar videos using vector search
        // Add 1 to limit because the source video might be included
        return videoDao.findByVector(sourceVector, limit + 1)
            .all()
            .stream()
            .filter(video -> !video.getVideoId().equals(videoId)) // Exclude source video
            .limit(limit)
            .map(video -> {
                RecommendationResponse recommendation = new RecommendationResponse();
                recommendation.setVideo(VideoResponse.fromVideo(video));
                return recommendation;
            })
            .toList();
    }
} 