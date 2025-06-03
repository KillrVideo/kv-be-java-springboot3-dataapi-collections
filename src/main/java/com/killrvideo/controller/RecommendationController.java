package com.killrvideo.controller;

import com.killrvideo.dto.RecommendationResponse;
import com.killrvideo.service.RecommendationService;
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

import java.util.List;

@RestController
@RequestMapping("/recommendations") // Relative to /api/v1 context path
@Tag(name = "Recommendations", description = "Video recommendation endpoints")
public class RecommendationController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

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
} 