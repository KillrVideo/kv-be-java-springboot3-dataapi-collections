package com.killrvideo.controller;

import com.killrvideo.dao.UserDao;
import com.killrvideo.dto.JwtResponse;
import com.killrvideo.dto.LoginRequest;
import com.killrvideo.dto.SignupRequest;
import com.killrvideo.dto.UpdateUserRequest;
import com.killrvideo.dto.User;
import com.killrvideo.security.JwtUtils;
import com.killrvideo.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Processing signin request for user: {}", loginRequest.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        logger.info("User authenticated successfully: {}", userDetails.getUsername());
        JwtResponse response = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername());
        logger.info("Token created.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Processing signup request for user: {}", signUpRequest.getEmail());

        if (userDao.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("Email is already in use: {}", signUpRequest.getEmail());
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(
                UUID.randomUUID().toString(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                Instant.now(),
                "USER"
        );

        userDao.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        return ResponseEntity.ok("User registered successfully!");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest updateRequest) {
        
        logger.info("Processing update request for user: {}", userId);

        // Find the user in the database
        Optional<User> userOptional = userDao.findByUserId(userId);
        if (userOptional.isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity
                    .badRequest()
                    .body("Error: User not found!");
        }

        User user = userOptional.get();

        // Update only the fields that are present in the request
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            // Check if the new email is already in use
            if (userDao.existsByEmail(updateRequest.getEmail())) {
                logger.warn("Email is already in use: {}", updateRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body("Error: Email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPassword() != null) {
            user.setHashedPassword(encoder.encode(updateRequest.getPassword()));
        }

        try {
            userDao.update(user);
            logger.info("User updated successfully: {}", user.getEmail());
            return ResponseEntity.ok("User updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body("Error updating user: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        logger.info("Processing get request for user: {}", userId);

        Optional<User> userOptional = userDao.findByUserId(userId);
        if (userOptional.isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.badRequest().body("Error: User not found!");
        }

        User user = userOptional.get();
        return ResponseEntity.ok(user);
    }
} 