package com.killrvideo.dao;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Sort;

import com.killrvideo.dto.Flag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ModerationDao {
    private static final Logger logger = LoggerFactory.getLogger(ModerationDao.class);
    private final Collection<Flag> moderationCollection;

    @Autowired
    public ModerationDao(Database killrVideoDatabase) {
        this.moderationCollection = killrVideoDatabase.getCollection("content_moderation", Flag.class);
        logger.info("Initialized ModerationDao with 'content_moderation' collection");
    }

    /**
     * Creates a new moderation flag.
     * If the flagId is not set, generates a new UUID.
     *
     * @param flag The flag to create
     * @return The created flag
     */
    public Flag createFlag(Flag flag) {
        if (flag.getFlagId() == null) {
            flag.setFlagId(UUID.randomUUID().toString());
            logger.debug("Generated new flag ID: {}", flag.getFlagId());
        }
        moderationCollection.insertOne(flag);
        logger.debug("Created flag with ID: {}", flag.getFlagId());
        return flag;
    }

    /**
     * Gets flag details by flagId.
     *
     * @param flagId The ID of the flag to retrieve
     * @return Optional containing the flag if found, empty otherwise
     */
    public Optional<Flag> getFlagById(String flagId) {
        logger.debug("Getting flag by ID: {}", flagId);
        return moderationCollection.findOne(Filters.eq("flag_id", flagId));
    }

    /**
     * Gets moderation flags, optionally filtered by status.
     *
     * @param status (Optional) The status to filter by. If null, returns all flags.
     * @param limit Maximum number of flags to return
     * @return List of flags
     */
    public List<Flag> getFlags(String status, int limit) {
        logger.debug("Getting moderation flags. Status: {}, Limit: {}", status, limit);
        if (status != null && !status.trim().isEmpty()) {
            return moderationCollection.find(
                Filters.eq("status", status),
                new CollectionFindOptions().sort(Sort.descending("created_at")).limit(limit)
            ).toList();
        } else {
            return moderationCollection.find(
                null,
                new CollectionFindOptions().sort(Sort.descending("created_at")).limit(limit)
            ).toList();
        }
    }

    /**
     * Updates an existing flag document.
     *
     * @param flag The flag to update
     * @throws IllegalArgumentException if flag ID is null
     */
    public void updateFlag(Flag flag) {
        if (flag.getFlagId() == null) {
            logger.error("Attempted to update flag with null ID");
            throw new IllegalArgumentException("Flag ID cannot be null for update");
        }
        logger.debug("Updating flag with ID: {}", flag.getFlagId());
        moderationCollection.replaceOne(Filters.eq("flag_id", flag.getFlagId()), flag);
    }
} 