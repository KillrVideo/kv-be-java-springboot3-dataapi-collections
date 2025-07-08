package com.killrvideo.controller;

import com.killrvideo.dao.ModerationDao;
import com.killrvideo.dao.UserDao;
import com.killrvideo.dto.User;
import com.killrvideo.dto.Flag;
import com.killrvideo.dto.FlagCreateRequest;
import com.killrvideo.dto.FlagUpdateRequest;
import com.killrvideo.dto.UpdateUserRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ModerationController {
    private static final Logger logger = LoggerFactory.getLogger(ModerationController.class);

    @Autowired
    private ModerationDao moderationDao;

    @Autowired
    private UserDao userDao;

    /**
     * GET /moderation/flags?status=...&limit=...
     */
    @GetMapping("/moderation/flags")
    public ResponseEntity<List<Flag>> getModerationFlags(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit <= 0 || limit > 100) limit = 20;
        try {
            List<Flag> flags = moderationDao.getFlags(status, limit);
            return ResponseEntity.ok(flags);
        } catch (Exception e) {
            logger.error("Error fetching moderation flags: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /moderation/flags/{flagId}
     */
    @GetMapping("/moderation/flags/{flagId}")
    public ResponseEntity<Flag> getFlagDetails(@PathVariable String flagId) {
        try {
            Optional<Flag> flagOpt = moderationDao.getFlagById(flagId);
            return flagOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching flag details for {}: {}", flagId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /moderation/flags/{flagId}/action
     */
    @PostMapping("/moderation/flags/{flagId}/action")
    public ResponseEntity<?> updateFlag(
            @PathVariable String flagId,
            @RequestBody FlagUpdateRequest updateRequest) {
        try {
            Optional<Flag> flagOpt = moderationDao.getFlagById(flagId);
            if (flagOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Flag flag = flagOpt.get();
            if (updateRequest.getStatus() != null) {
                flag.setStatus(updateRequest.getStatus());
            }
            if (updateRequest.getModeratorNotes() != null) {
                // If you add moderatorNotes to Flag DTO, set it here
                // flag.setModeratorNotes(updateRequest.getModeratorNotes());
            }
            flag.setUpdatedAt(Instant.now().toString());
            moderationDao.updateFlag(flag);
            return ResponseEntity.ok(flag);
        } catch (Exception e) {
            logger.error("Error updating flag {}: {}", flagId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /flags
     */
    @PostMapping("/flags")
    public ResponseEntity<Flag> createFlag(@RequestBody FlagCreateRequest createRequest) {
        try {
            Flag flag = new Flag();
            flag.setFlagId(UUID.randomUUID().toString());
            flag.setContentType(createRequest.getContentType());
            flag.setContentId(createRequest.getContentId());
            flag.setReasonCode(createRequest.getReasonCode());
            flag.setReasonText(createRequest.getReasonText());
            flag.setCreatedAt(Instant.now().toString());
            flag.setUpdatedAt(flag.getCreatedAt());
            flag.setStatus("OPEN");
            // Optionally set userId if available from auth context
            moderationDao.createFlag(flag);
            return ResponseEntity.status(HttpStatus.CREATED).body(flag);
        } catch (Exception e) {
            logger.error("Error creating flag: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /moderation/users/{userId}
     */
    @GetMapping("/moderation/users/{userId}")
    public ResponseEntity<User> searchUsers(@PathVariable String userId) {
        try {
            Optional<User> userOpt = userDao.findByUserId(userId);
            return userOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching user details for {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("moderation/users/{userId}/assign-moderator")
    public ResponseEntity<?> assignModerator(@PathVariable String userId, @RequestBody UpdateUserRequest assignRequest) {
        try {
            Optional<User> userOpt = userDao.findByUserId(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            user.setRoles(assignRequest.getRoles());
            userDao.update(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error assigning moderator for {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/moderation/users")
    public ResponseEntity<?> searchUsers(HttpServletRequest request) {
        try {
            int limit = 20;
            Map<String, String[]> params = request.getParameterMap();
            String query = params.get("query")[0];

            List<User> users = userDao.searchUsers(query,limit);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 