package com.killrvideo.controller;

import com.killrvideo.dao.VideoDao;
import com.killrvideo.dao.RatingDao;
import com.killrvideo.dto.*;
import com.killrvideo.security.UserDetailsImpl;
import com.killrvideo.service.StorageService;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/videos") // Relative to /api/v1 context path
public class VideoController {
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private RatingDao ratingDao;

    private StorageService storageService = new StorageService();

    private static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    /**
     * Submit a new video
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitVideo(
            @Valid @RequestBody VideoSubmitRequest submitRequest) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String userId = userDetails.getUserId();

        try {
            // Create video metadata
            Video video = new Video();
            video.setVideoId(UUID.randomUUID().toString());
            video.setUserId(userId);
            video.setName(submitRequest.getTitle() != null ? submitRequest.getTitle() : "Untitled Video");
            video.setDescription(null); // Will be updated during processing
            video.setTags(null); // Will be updated during processing
            video.setLocation(submitRequest.getYoutubeUrl());
            video.setAddedDate(Instant.now());
            video.setProcessingStatus("PENDING");
            
            // Generate the embedding for the video
            String videoText = video.getName();
            float[] videoVector = embeddingModel.embed(videoText).content().vector();
            video.setVector(videoVector);

            video.setPreviewImageLocation(null);

            Video savedVideo = videoDao.save(video);
            
            VideoResponse response = VideoResponse.fromVideo(savedVideo);
            response.setProcessingStatus("PENDING");
            
            logger.info("Video submitted successfully. ID: {}", savedVideo.getVideoId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid video submission: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing video submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing video submission");
        }
    }

    /**
     * Get video status
     */
    @GetMapping("/id/{videoId}/status")
    public ResponseEntity<VideoStatusResponse> getVideoStatus(@PathVariable String videoId) {
        return videoDao.findByVideoId(videoId, false)
                .map(video -> {
                    VideoStatusResponse response = new VideoStatusResponse();
                    response.setVideoId(video.getVideoId());
                    response.setStatus(video.getProcessingStatus());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get video details
     */
    @GetMapping("/id/{videoId}")
    public ResponseEntity<VideoResponse> getVideoDetails(@PathVariable String videoId) {
        return videoDao.findByVideoId(videoId, false)
                .map(video -> {
                    VideoResponse response = VideoResponse.fromVideo(video);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Record a video view
     */
    //POST /api/v1/videos/id/900c1236-55ae-4f05-a7fb-d566d603a2ae/view
    @PostMapping("/id/{videoId}/view")
    public ResponseEntity<?> recordVideoView(@PathVariable String videoId) {
        Optional<Video> videoOpt = videoDao.findByVideoId(videoId, false);

        if (videoOpt.isPresent()) {
            Video video = videoOpt.get();
            long views = video.getViews() + 1;
            video.setViews(views);
            videoDao.updateViews(videoId,views);
            return ResponseEntity.ok(VideoResponse.fromVideo(video));
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get latest videos
     */
    @GetMapping("/latest")
    public ResponseEntity<LatestVideosResponse> getLatestVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page <= 0 || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        List<Video> videoList = videoDao.findLatest(pageSize);

        List<VideoResponse> videos = new ArrayList<>();
        for (Video video : videoList) {
            VideoResponse videoResponse = VideoResponse.fromVideo(video);
            // Get all ratings for the video
            List<Rating> ratings = ratingDao.findByVideoId(video.getVideoId());

            if (ratings.size() > 0) {
                int ratingCount = ratings.size();
                int totalRating = 0;
                for (Rating rating : ratings) {
                    totalRating += rating.getRatingAsInt();
                }

                videoResponse.setRating(totalRating / ratingCount);
            } else {
                videoResponse.setRating(0.0f);
            }
            
            videos.add(videoResponse);
        }

        LatestVideosResponse response = new LatestVideosResponse(videos);

        return ResponseEntity.ok(response);
    }

    /**
     * Get videos by uploader
     */
    @GetMapping("/by-uploader/{uploaderId}")
    public ResponseEntity<List<VideoResponse>> getVideosByUploader(
            @PathVariable String uploaderId,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        List<VideoResponse> videos = videoDao.findByUserId(uploaderId, pageSize)
                .stream()
                .map(VideoResponse::fromVideo)
                .toList();
        return ResponseEntity.ok(videos);
    }

    /**
     * Get videos by tag
     */
    @GetMapping("/by-tag/{tagName}")
    public ResponseEntity<List<VideoResponse>> getVideosByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageSize <= 0 || pageSize > 50) {
            pageSize = 10;
        }
        if (tagName == null || tagName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<VideoResponse> videos = videoDao.findByTag(tagName.trim(), pageSize)
                .stream()
                .map(VideoResponse::fromVideo)
                .toList();
        return ResponseEntity.ok(videos);
    }

    /**
     * Update video details
     */
    @PutMapping("/id/{videoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateVideo(
            @PathVariable String videoId,
            @Valid @RequestBody VideoUpdateRequest updateRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return videoDao.findByVideoId(videoId, false)
                .map(video -> {
                    // Check if the authenticated user owns the video
                    if (!video.getUserId().equals(userDetails.getUserId())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("You are not authorized to update this video");
                    }

                    // Update only the fields that are present in the request
                    if (updateRequest.getName() != null) {
                        video.setName(updateRequest.getName());
                    }
                    if (updateRequest.getDescription() != null) {
                        video.setDescription(updateRequest.getDescription());
                    }
                    if (updateRequest.getTags() != null) {
                        video.setTags(updateRequest.getTags());
                    }

                    videoDao.update(video);
                    return ResponseEntity.ok(VideoResponse.fromVideo(video));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Find similar videos based on vector similarity
     */
    @GetMapping("/id/{videoId}/related")
    public ResponseEntity<List<VideoResponse>> getSimilarVideos(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "5") int requestedLimit) {
        final int limit = requestedLimit <= 0 || requestedLimit > 20 ? 5 : requestedLimit;

        Optional<Video> sourceVideoOpt = videoDao.findByVideoId(videoId, true);

        if (sourceVideoOpt.isPresent()) {
            Video sourceVideo = sourceVideoOpt.get();

            List<VideoResponse> similarVideos = videoDao.findByVector(sourceVideo.getVector(), limit + 1)
                .stream()
                .filter(video -> !video.getVideoId().equals(videoId))
                .limit(limit)
                .map(VideoResponse::fromVideo)
                .toList();
            return ResponseEntity.ok(similarVideos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 