package com.killrvideo.controller;

import com.killrvideo.dao.VideoDao;
import com.killrvideo.dao.UserDao;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/videos") // Relative to /api/v1 context path
public class VideoController {
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private UserDao userDao;

    private StorageService storageService = new StorageService();

    private static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    /**
     * Submit a new video
     */
    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) Set<String> tags,
            @PathVariable String userId) {

        User userDetails = userDao.findByUserId(userId).get();

        if (userDetails == null) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.badRequest().body("Error: User not found!");
        }

        try {
            // Store the video file
            String filename = storageService.store(file);
            
            // Create video metadata
            Video video = new Video();
            video.setVideoId(UUID.randomUUID().toString());
            video.setUserId(userId);
            video.setName(name);
            video.setDescription(description);
            video.setTags(tags);
            video.setLocation(filename); // Store the filename as the location
            video.setAddedDate(Instant.now());
            
            // Generate the embedding for the video
            String videoText = video.getName() + " " + (video.getDescription() != null ? video.getDescription() : "");
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
    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getVideoByVideoId(@PathVariable String videoId) {
        return videoDao.findByVideoId(videoId, false)
                .map(video -> {
                    VideoResponse response = VideoResponse.fromVideo(video);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get latest videos
     */
    @GetMapping("/latest/page/{page}/page_size/{pageSize}")
    public ResponseEntity<List<VideoResponse>> getLatestVideos(
            @PathVariable int page,
            @PathVariable int pageSize) {
        if (page <= 0 || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        List<VideoResponse> videos = videoDao.findLatest(pageSize)
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
    @GetMapping("{videoId}/related")
    public ResponseEntity<List<VideoResponse>> getSimilarVideos(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "5") int requestedLimit) {
        final int limit = requestedLimit <= 0 || requestedLimit > 20 ? 5 : requestedLimit;

        Optional<Video> sourceVideoOpt = videoDao.findByVideoId(videoId, true);

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
                    if (!video.getUserId().equals(userDetails.getUserId())) {
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