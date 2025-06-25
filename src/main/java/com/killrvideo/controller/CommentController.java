package com.killrvideo.controller;

import com.killrvideo.dao.CommentDao;
import com.killrvideo.dao.UserDao;
import com.killrvideo.dao.VideoDao;
import com.killrvideo.dto.Comment;
import com.killrvideo.dto.User;
import com.killrvideo.dto.CommentResponse;
import com.killrvideo.dto.SubmitCommentRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/comments") // Relative to /api/v1 context path
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private VideoDao videoDao; // To verify video existence

    @Autowired
    private UserDao userDao;

    /**
     * Submit a new comment
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> submitComment(
            @Valid @RequestBody SubmitCommentRequest submitCommentRequest) {
        // UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // String userId = userDetails.getId();

        // Verify video exists
        if (!videoDao.findByVideoId(submitCommentRequest.getVideoId(), false).isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        Comment comment = new Comment();
        comment.setCommentId(UUID.randomUUID().toString());
        comment.setVideoId(submitCommentRequest.getVideoId());
        comment.setUserId(submitCommentRequest.getUserId());
        comment.setComment(submitCommentRequest.getCommentText());
        comment.setTimestamp(Instant.now());

        Comment savedComment = commentDao.save(comment);
        
        CommentResponse response = CommentResponse.fromComment(savedComment);
        
        logger.info("Comment submitted successfully. ID: {}", savedComment.getCommentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get comments for a video
     */
    @GetMapping("/id/{videoId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByVideo(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }

        List<CommentResponse> comments = commentDao.findByVideoId(videoId, limit)
                .stream()
                .map(CommentResponse::fromComment)
                .toList();

        return ResponseEntity.ok(comments);
    }

    /**
     * Get comments by a user
     */
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }

        List<CommentResponse> comments = commentDao.findByUserId(userId, limit)
                .stream()
                .map(CommentResponse::fromComment)
                .toList();

        return ResponseEntity.ok(comments);
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/{commentId}/userid/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
        @PathVariable String commentId,
        @PathVariable String userId) {

        Optional<Comment> comment = commentDao.findByCommentId(commentId);

        // check if the comment exists
        if (comment.isPresent()) {
            // Check if the authenticated user owns the comment
            if (comment.get().getUserId().equals(userId)) { 
                commentDao.deleteByCommentId(commentId);
            } else {
                Optional<User> user = userDao.findByUserId(userId);

                if (user.get().getRole().equals("ADMIN")) {
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
} 