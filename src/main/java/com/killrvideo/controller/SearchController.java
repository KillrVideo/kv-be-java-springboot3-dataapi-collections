package com.killrvideo.controller;

import com.killrvideo.dao.VideoDao;
import com.killrvideo.dto.VideoResponse;
import com.killrvideo.dto.TagSuggestion;
import com.killrvideo.dto.Video;
import com.killrvideo.dto.SearchVideosResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/search")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    // Hugging Face's All-Mini-LmL6-V2 embedding model was used to generate the embeddings for the videos.
    // This model is a small, fast, and accurate embedding model that is suitable for our use case.
    private static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Autowired
    private VideoDao videoDao;

    /**
     * Get tag suggestions based on a query
     */
    @GetMapping("/tags/suggest")
    public ResponseEntity<List<TagSuggestion>> suggestTags(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Empty query provided for tag suggestions");
            return ResponseEntity.badRequest().body(List.of());
        }

        if (limit <= 0 || limit > 50) {
            limit = 10;
        }

        try {
            List<String> tagSuggestions = videoDao.suggestTags(query.trim(), limit);
            
            List<TagSuggestion> response = tagSuggestions.stream()
                .map(tag -> {
                    TagSuggestion suggestion = new TagSuggestion();
                    suggestion.setTag(tag);
                    return suggestion;
                })
                .collect(Collectors.toList());

            logger.debug("Found {} tag suggestions for query: {}", response.size(), query);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting tag suggestions for query: {}", query, e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    /**
     * Search videos by query string
     */
    @GetMapping("/videos")
    public ResponseEntity<?> searchVideos(
            HttpServletRequest request) {

        Integer limit = 20;
        Map<String, String[]> params = request.getParameterMap();

        String query = params.get("query")[0];
        if (params.containsKey("limit")) {
            limit = Integer.parseInt(params.get("limit")[0]);
        }

        if (query == null || query.trim().isEmpty()) {
            logger.warn("Empty query string provided for video search");
            return ResponseEntity.badRequest().body(List.of());
        }

        try {
            float[] queryEmbeddings = embeddingModel.embed(query).content().vector();
            
            Optional<List<Video>> searchResults = videoDao.searchVideos(queryEmbeddings, limit);
            
            if (searchResults.isEmpty()) {
                logger.debug("No search results found for query: {}", query);
                return ResponseEntity.ok(List.of());
            }

            List<VideoResponse> videos = searchResults.get()
                .stream()
                .map(VideoResponse::fromVideo)
                .collect(Collectors.toList());

            logger.debug("Found {} videos for query: {}", videos.size(), query);

            SearchVideosResponse response = new SearchVideosResponse(videos);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching videos for query: {}", query, e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
} 