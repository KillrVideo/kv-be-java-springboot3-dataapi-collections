package com.killrvideo.controller;

import com.killrvideo.dao.VideoDao;
import com.killrvideo.dto.*;
import com.killrvideo.security.UserDetailsImpl;

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

import java.time.Instant;
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

    private static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    /**
     * Submit a new video
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoResponse> submitVideo(
            @Valid @RequestBody SubmitVideoRequest submitVideoRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        Video video = new Video();
        video.setVideoId(UUID.randomUUID().toString());
        video.setUserId(userId);
        video.setName(submitVideoRequest.getName());
        video.setDescription(submitVideoRequest.getDescription());
        video.setTags(submitVideoRequest.getTags());
        video.setLocation(submitVideoRequest.getLocation());
        video.setAddedDate(Instant.now());
        
        // generate the embedding for the video
        String videoText = video.getName() + " " + video.getDescription();
        float[] videoVector = embeddingModel.embed(videoText).content().vector();
        video.setVector(videoVector);

        video.setPreviewImageLocation(null);
        video.setVector(null);

        Video savedVideo = videoDao.save(video);
        
        VideoResponse response = VideoResponse.fromVideo(savedVideo);
        response.setProcessingStatus("PENDING");
        
        logger.info("Video submitted successfully. ID: {}", savedVideo.getVideoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a video by databaseID
     */
    @GetMapping("/database/{videoId}")
    public ResponseEntity<VideoResponse> getVideoByDatabaseId(@PathVariable String videoId) {
        return videoDao.findById(videoId)
                .map(video -> {
                    VideoResponse response = VideoResponse.fromVideo(video);
                    // TODO: Fetch and set additional metadata (user name, comment count)
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a video by videoId
     */
    @GetMapping("/video/{videoId}")
    public ResponseEntity<VideoResponse> getVideoByVideoId(@PathVariable String videoId) {
        return videoDao.findByVideoId(videoId)
                .map(video -> {
                    VideoResponse response = VideoResponse.fromVideo(video);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get latest videos
     */
    @GetMapping("/latest")
    public ResponseEntity<List<VideoResponse>> getLatestVideos(
            @RequestParam(defaultValue = "15") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 10;
        }
        List<VideoResponse> videos = videoDao.findLatest(limit)
                .all()
                .stream()
                .map(VideoResponse::fromVideo)
                .toList();
        return ResponseEntity.ok(videos);
    }

    /**
     * Get videos by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VideoResponse>> getVideosByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 10;
        }
        List<VideoResponse> videos = videoDao.findByUserId(userId, limit)
                .all()
                .stream()
                .map(VideoResponse::fromVideo)
                .toList();
        return ResponseEntity.ok(videos);
    }

    /**
     * Update a video
     */
    @PutMapping("/{videoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateVideo(
            @PathVariable String videoId,
            @Valid @RequestBody VideoUpdateRequest updateRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return videoDao.findById(videoId)
                .map(video -> {
                    // Check if the authenticated user owns the video
                    if (!video.getUserId().equals(userDetails.getId())) {
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
     * Search videos by tag
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<VideoResponse>> getVideosByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "10") int limit) {
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }
        if (tag == null || tag.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<VideoResponse> videos = videoDao.findByTag(tag.trim(), limit)
                .all()
                .stream()
                .map(VideoResponse::fromVideo)
                .toList();
        return ResponseEntity.ok(videos);
    }

    /**
     * Find similar videos based on vector similarity
     */
    @GetMapping("/similar/{videoId}")
    public ResponseEntity<List<VideoResponse>> getSimilarVideos(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "5") int requestedLimit) {
        final int limit = requestedLimit <= 0 || requestedLimit > 20 ? 5 : requestedLimit;

        Optional<Video> sourceVideoOpt = videoDao.findByVideoId(videoId);

        if (sourceVideoOpt.isPresent()) {
            Video sourceVideo = sourceVideoOpt.get();

            List<VideoResponse> similarVideos = videoDao.findByVector(sourceVideo.getVector(), limit + 1)
                .all()
                .stream()
                .map(VideoResponse::fromVideo)
                .toList();
            return ResponseEntity.ok(similarVideos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Soft delete a video
     */
    @DeleteMapping("/{videoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteVideo(
            @PathVariable String videoId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return videoDao.findById(videoId)
                .map(video -> {
                    if (!video.getUserId().equals(userDetails.getId())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("You are not authorized to delete this video");
                    }

                    video.setDeleted(true);
                    video.setDeletedAt(Instant.now());
                    videoDao.update(video);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 