package com.killrvideo.controller;

import com.killrvideo.dao.VideoDao;
import com.killrvideo.dao.RatingDao;
import com.killrvideo.dao.CommentDao;
import com.killrvideo.dao.UserDao;
import com.killrvideo.dto.*;
import com.killrvideo.security.UserDetailsImpl;
//import com.killrvideo.service.StorageService;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import io.jsonwebtoken.lang.Arrays;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/videos") // Relative to /api/v1 context path
public class VideoController {
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    // A collection of regex patterns that match the majority of YouTube URL formats
    // and capture the video ID in a named group called "id".
    private List<Pattern> _YOUTUBE_PATTERNS = new ArrayList<>();

    @Value("${killrvideo.youtube.api-key}")
    private String YOUTUBE_API_KEY;

    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id={YOUTUBE_ID}&key={API_KEY}";

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private RatingDao ratingDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommentDao commentDao;

//    private StorageService storageService = new StorageService();

    //private static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    private static EmbeddingModel embeddingModel = HuggingFaceEmbeddingModel.builder()
			.accessToken(System.getenv("HF_API_KEY"))
			.baseUrl("https://router.huggingface.co/hf-inference/")
			.modelId("ibm-granite/granite-embedding-30m-english")
			.waitForModel(true)
			.build();

    public VideoController() {
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtu\\.be/(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/watch\\?v=(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/embed/(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/v/(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/shorts/(?<id>[A-Za-z0-9_-]{11})"));
    }

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
            // Create video with properties from request
            Video video = new Video();

            Set<String> tags = new HashSet<>(Arrays.asList(submitRequest.getTags()));

            video.setDescription(submitRequest.getDescription());
            video.setTags(tags);
            video.setLocation(submitRequest.getYoutubeUrl());

            // parse youtube id from url
            String youtubeId = extractYouTubeId(submitRequest.getYoutubeUrl());
            video.setYoutubeId(youtubeId);

            // fetch youtube metadata
            YoutubeMetadata youtubeMetadata = fetchYoutubeMetadata(youtubeId);

            logger.info("Youtube metadata.title: {}", youtubeMetadata.getTitle());
            logger.info("Youtube metadata.thumbnailUrl: {}", youtubeMetadata.getThumbnailUrl());

            video.setName(youtubeMetadata.getTitle());
            video.setPreviewImageLocation(youtubeMetadata.getThumbnailUrl());

            // generate remaining properties
            video.setAddedDate(Instant.now());
            //video.setLastViewed(Instant.now().toString());
            video.setProcessingStatus("PENDING");
            video.setVideoid(UUID.randomUUID().toString());
            video.setUserid(userId);
            
            // Generate the embedding for the video
            String videoText = video.getName();
            float[] videoVector = embeddingModel.embed(videoText).content().vector();
            video.setVector(videoVector);

            // save video to database
            Video savedVideo = videoDao.save(video);
            
            VideoResponse response = VideoResponse.fromVideo(savedVideo);
            response.setProcessingStatus("PENDING");
            
            logger.info("Video submitted successfully. ID: {}", savedVideo.getVideoid());
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
                    response.setVideoId(video.getVideoid());
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
        Optional<Video> video = videoDao.findByVideoId(videoId, false);
        if (video.isPresent()) {
        	VideoResponse response = VideoResponse.fromVideo(video.get());
        	
        	if (response.getYoutubeVideoId() == null || response.getYoutubeVideoId().isEmpty()) {
        		response.setYoutubeVideoId(extractYouTubeId(response.getLocation()));
        	}
        	
        	return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.notFound().build();
    }

    /**
     * Record a video view
     */
    //POST /api/v1/videos/id/900c1236-55ae-4f05-a7fb-d566d603a2ae/view
    @PostMapping("/id/{videoId}/view")
    public ResponseEntity<?> recordVideoView(@PathVariable String videoId) {
        Optional<Video> videoOpt = videoDao.findByVideoId(videoId, true);

        if (videoOpt.isPresent()) {
            Instant now = Instant.now();
            Video video = videoOpt.get();
            int views = video.getStats().getViews() + 1;
            video.getStats().setViews(views);
            //video.setLastViewed(now.toString());
            //videoDao.updateViews(videoId,views,now);
            videoDao.update(video);
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
            List<Rating> ratings = ratingDao.findByVideoId(video.getVideoid());

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
     * Get trending videos
     */
    @GetMapping("/trending")
    public ResponseEntity<List<VideoResponse>> getTrendingVideos(
            @RequestParam(defaultValue = "1") int days, @RequestParam(defaultValue = "10") int limit) {
        List<String> uniqueVideoIDs = new ArrayList<>();
        List<Video> videos = videoDao.findTrending(days, limit);
        List<VideoResponse> videoResponses = new ArrayList<>();

        for (Video video : videos) {
            uniqueVideoIDs.add(video.getVideoid());
            VideoResponse videoResponse = VideoResponse.fromVideo(video);
            videoResponses.add(videoResponse);
        }

        if (videoResponses.size() < limit) {
            // if we can't meet the limit from trending, then get more from latest
            List<Video> moreVideos = videoDao.findLatest(limit * 2);
            for (Video video : moreVideos) {
                if (!uniqueVideoIDs.contains(video.getVideoid())) {
                    VideoResponse videoResponse = VideoResponse.fromVideo(video);
                    videoResponses.add(videoResponse);
                }
            }
        }

        return ResponseEntity.ok(videoResponses);
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
            @Valid @RequestBody VideoUpdateRequest updateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        
        return videoDao.findByVideoId(videoId, false)
                .map(video -> {
                    // Check if the authenticated user owns the video
                    if (!video.getUserid().equals(userDetails.getUserId())) {
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
                .filter(video -> !video.getVideoid().equals(videoId))
                .limit(limit)
                .map(VideoResponse::fromVideo)
                .toList();
            return ResponseEntity.ok(similarVideos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit a new comment
     */
    @PostMapping("{videoId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> submitComment(
            @PathVariable String videoId,
            @Valid @RequestBody SubmitCommentRequest submitCommentRequest) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String userId = userDetails.getUserId();        

        Comment comment = new Comment();
        comment.setCommentId(UUID.randomUUID().toString());
        comment.setVideoId(videoId);
        //comment.setUserId(submitCommentRequest.getUserId());
        comment.setUserId(userId);
        comment.setComment(submitCommentRequest.getCommentText());
        comment.setTimestamp(Instant.now());

        Optional<User> userOpt = userDao.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            comment.setUserName(user.getFirstName() + " " + user.getLastName());
        } else {
            int firstDashIndex = userId.indexOf('-');
            comment.setUserName(userId.substring(0, firstDashIndex));
        }

        Comment savedComment = commentDao.save(comment);
        
        CommentResponse response = CommentResponse.fromComment(savedComment);
        
        logger.info("Comment submitted successfully. ID: {}", savedComment.getCommentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get comments for a video
     * /videos/${videoId}/comments?page=${page}&pageSize=${pageSize}
     */
    @GetMapping("/{videoId}/comments")
    public ResponseEntity<CommentsDataResponse> getCommentsByVideo(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        // rudimentary limit calculation, for now
        int limit = pageSize * page;

        List<Comment> comments = commentDao.findByVideoId(videoId, limit);
        List<CommentResponse> commentRespList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentResponse commentResp = CommentResponse.fromComment(comment);

            Optional<User> userOpt = userDao.findByUserId(comment.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                commentResp.setFirstName(user.getFirstName());
                commentResp.setLastName(user.getLastName());
            } else {
                commentResp.setFirstName("anonymous");
                commentResp.setLastName("user");
            }

            commentRespList.add(commentResp);
        }

        CommentsDataResponse response = new CommentsDataResponse(commentRespList);

        return ResponseEntity.ok(response);
    }

        /**
     * Delete a comment
     */
    @DeleteMapping("/comment/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
        @PathVariable String commentId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String userId = userDetails.getUserId();

        Optional<Comment> comment = commentDao.findByCommentId(commentId);

        // check if the comment exists
        if (comment.isPresent()) {
            // Check if the authenticated user owns the comment
            if (comment.get().getUserId().equals(userId)) { 
                commentDao.deleteByCommentId(commentId);
            } else {
                Optional<User> user = userDao.findByUserId(userId);

                if (user.get().getRoles().equals("ADMIN")) {
                    commentDao.deleteByCommentId(commentId);
                } else {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body("You are not authorized to delete this comment");
                }    
            }
        }
        return ResponseEntity.ok().build();
    }

    private String extractYouTubeId(String youtubeUrl) {

        for (Pattern pattern : _YOUTUBE_PATTERNS) {
            Matcher match = pattern.matcher(youtubeUrl);
            if (match.find()) {
                return match.group("id");
            }
        }
        return null;
    }

    private YoutubeMetadata fetchYoutubeMetadata(String youtubeId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = YOUTUBE_API_URL
                    .replace("{YOUTUBE_ID}", youtubeId)
                    .replace("{API_KEY}", YOUTUBE_API_KEY);

            logger.debug("Fetching YouTube metadata for ID: {}", youtubeId);
            
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null) {
                logger.warn("Received null response from YouTube API for ID: {}", youtubeId);
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            
            // Check if the API returned any items
            JsonNode items = rootNode.get("items");
            if (items == null || items.size() == 0) {
                logger.warn("No video found for YouTube ID: {}", youtubeId);
                return null;
            }

            JsonNode videoInfo = items.get(0);
            JsonNode snippet = videoInfo.get("snippet");
            
            if (snippet == null) {
                logger.warn("No snippet found in YouTube API response for ID: {}", youtubeId);
                return null;
            }

            YoutubeMetadata metadata = new YoutubeMetadata();
            
            // Extract title
            if (snippet.has("title")) {
                metadata.setTitle(snippet.get("title").asText());
            }
            
            // Extract description
            if (snippet.has("description")) {
                metadata.setDescription(snippet.get("description").asText());
            }
            
            // Extract thumbnail URL (prefer high quality, fallback to default)
            if (snippet.has("thumbnails")) {
                JsonNode thumbnails = snippet.get("thumbnails");
                if (thumbnails.has("high")) {
                    metadata.setThumbnailUrl(thumbnails.get("high").get("url").asText());
                } else if (thumbnails.has("medium")) {
                    metadata.setThumbnailUrl(thumbnails.get("medium").get("url").asText());
                } else if (thumbnails.has("default")) {
                    metadata.setThumbnailUrl(thumbnails.get("default").get("url").asText());
                }
            }
            
            // Extract tags
            if (snippet.has("tags")) {
                JsonNode tagsNode = snippet.get("tags");
                List<String> tagsList = new ArrayList<>();
                for (JsonNode tag : tagsNode) {
                    tagsList.add(tag.asText());
                }
                metadata.setTags(tagsList.toArray(new String[0]));
            }

            logger.info("Successfully fetched YouTube metadata for ID: {}", youtubeId);
            return metadata;

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching YouTube metadata for ID {}: {}", youtubeId, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching YouTube metadata for ID {}: {}", youtubeId, e.getMessage());
            return null;
        }
    }
} 