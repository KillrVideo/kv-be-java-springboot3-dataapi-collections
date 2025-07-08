package com.killrvideo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public class StorageService {
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    
    private final Path rootLocation;

    @Value("${killrvideo.storage.allowed-content-types}")
    private List<String> allowedContentTypes;

    private String videoStorageLocation = "videos";

    public StorageService() {
        this.rootLocation = Paths.get(videoStorageLocation).toAbsolutePath().normalize();
        this.init();
        logger.info("Storage service initialized with root location: {}", this.rootLocation);
    }

    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                logger.info("Created video storage directory at: {}", rootLocation);
            } else {
                logger.info("Using existing video storage directory at: {}", rootLocation);
            }
        } catch (IOException e) {
            logger.error("Could not initialize storage location: {}", e.getMessage());
            throw new RuntimeException("Could not initialize storage location: " + rootLocation, e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                logger.error("Failed to store empty file");
                throw new IllegalArgumentException("Failed to store empty file");
            }

            String contentType = file.getContentType();
            logger.debug("Received file with content type: {}", contentType);

            if (!allowedContentTypes.contains(contentType)) {
                logger.error("File type not allowed: {}", contentType);
                throw new IllegalArgumentException("File type not allowed. Allowed types: " + allowedContentTypes);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + extension;
            
            Path destinationFile = rootLocation.resolve(filename).normalize();

            // Security check
            if (!destinationFile.getParent().equals(rootLocation)) {
                logger.error("Cannot store file outside current directory: {}", destinationFile);
                throw new IllegalArgumentException("Cannot store file outside current directory");
            }

            logger.debug("Storing file {} to {}", originalFilename, destinationFile);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Successfully stored file: {} as {}", originalFilename, filename);

            return filename;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage());
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            
            // Security check
            if (!file.getParent().equals(rootLocation)) {
                logger.error("Cannot delete file outside storage directory: {}", file);
                throw new IllegalArgumentException("Cannot delete file outside storage directory");
            }

            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                logger.info("Successfully deleted file: {}", filename);
            } else {
                logger.warn("File not found for deletion: {}", filename);
            }
        } catch (IOException e) {
            logger.error("Failed to delete file {}: {}", filename, e.getMessage());
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }
} 