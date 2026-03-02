package com.killrvideo.controller;

import com.killrvideo.dto.RecommendationResponse;
import com.killrvideo.dto.Video;
import com.killrvideo.dto.VideoResponse;
import com.killrvideo.service.RecommendationService;
import com.killrvideo.dao.VideoDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recommendations") // Relative to /api/v1 context path
@Tag(name = "Recommendations", description = "Video recommendation endpoints")
public class RecommendationController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private VideoDao videoDao;
    
    @Autowired
    private RecommendationService recommendationService;

    /**
     * Get similar video recommendations
     */
    @GetMapping("/similar/{videoId}")
    @Operation(summary = "Get similar video recommendations",
            description = "Returns a list of videos similar to the given video, based on content similarity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recommendations"),
            @ApiResponse(responseCode = "400", description = "Invalid video ID or limit parameter"),
            @ApiResponse(responseCode = "404", description = "Video not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RecommendationResponse>> getSimilarVideos(
            @Parameter(description = "ID of the video to find similar content for")
            @PathVariable String videoId,
            @Parameter(description = "Maximum number of recommendations to return (default: 10, max: 20)")
            @RequestParam(defaultValue = "10") int limit) {
        
        // Validate limit parameter
        if (limit <= 0 || limit > 20) {
            limit = 10;
        }

        try {
            List<RecommendationResponse> recommendations = 
                recommendationService.getSimilarVideos(videoId, limit);
            
            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for video recommendations: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Error getting video recommendations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("Unexpected error in recommendation endpoint: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/foryou")
    public ResponseEntity<List<RecommendationResponse>> getForYouVideos() {
    	
    	List<RecommendationResponse> returnVal = new ArrayList<>();

    	// use findLatest() for now
    	List<Video> recommendedVideoList = videoDao.findLatest(5);
    	
    	for (Video recVideo : recommendedVideoList) {
    		RecommendationResponse recResponse = new RecommendationResponse();
    		VideoResponse videoResp = new VideoResponse();
    		
    		videoResp.setAddedDate(recVideo.getAddedDate());
    		videoResp.setDescription(recVideo.getDescription());
    		videoResp.setKey(recVideo.getVideoid());
    		videoResp.setLocation(recVideo.getLocation());
    		videoResp.setName(recVideo.getName());
    		videoResp.setPreviewImageLocation(recVideo.getPreviewImageLocation());
    		videoResp.setProcessingStatus(recVideo.getProcessingStatus());
    		videoResp.setTags(recVideo.getTags());
    		videoResp.setUserId(recVideo.getUserid());
    		videoResp.setVector(recVideo.getVector());
    		videoResp.setVideoId(recVideo.getVideoid());
    		videoResp.setYoutubeVideoId(recVideo.getYoutubeId());
    		
    		// add video response to recommended response
    		recResponse.setVideo(videoResp);
    		recResponse.setSimilarityScore(0.0d);
    		
    		// add recommended response to returnval
    		returnVal.add(recResponse);
    	}
    	
    	return ResponseEntity.ok(returnVal);
    }
} 