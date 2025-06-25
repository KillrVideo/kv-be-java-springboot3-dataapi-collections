package com.killrvideo.dao;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Sort;

import com.killrvideo.dto.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommentDao {
    private static final Logger logger = LoggerFactory.getLogger(CommentDao.class);
    private final Collection<Comment> commentCollection;

    @Autowired
    public CommentDao(Database killrVideoDatabase) {
        this.commentCollection = killrVideoDatabase.getCollection("comments", Comment.class);
        logger.info("Initialized CommentDao with 'comments' collection");
    }

    /**
     * Saves a new comment document to the database.
     * If the commentId is not set, generates a new UUID.
     *
     * @param comment The comment to save
     * @return The saved comment
     */
    public Comment save(Comment comment) {
        if (comment.getCommentId() == null) {
            comment.setCommentId(UUID.randomUUID().toString());
            logger.debug("Generated new comment ID: {}", comment.getCommentId());
        }
        commentCollection.insertOne(comment);
        logger.debug("Saved comment with ID: {}", comment.getCommentId());
        return comment;
    }

    /**
     * Finds a comment by its ID.
     *
     * @param commentId The ID of the comment to find
     * @return Optional containing the comment if found, empty otherwise
     */
    public Optional<Comment> findById(String commentId) {
        logger.debug("Finding comment by ID: {}", commentId);
        return commentCollection.findById(commentId);
    }

    public Optional<Comment> findByCommentId(String commentId) {
        logger.debug("Finding comment by ID: {}", commentId);
        return commentCollection.findOne(Filters.eq("comment_id", commentId));
    }

    /**
     * Finds comments for a specific video, sorted by timestamp in descending order.
     *
     * @param videoId The ID of the video
     * @param limit Maximum number of comments to return
     * @return Iterable of comments
     */
    public List<Comment> findByVideoId(String videoId, int limit) {
        logger.debug("Finding comments for video: {}, limit: {}", videoId, limit);
        return commentCollection.find(
            Filters.eq("video_id", videoId),
            new CollectionFindOptions().sort(Sort.descending("timestamp"))
            .limit(limit))
            .toList();
    }

    /**
     * Finds comments by a specific user, sorted by timestamp in descending order.
     *
     * @param userId The ID of the user
     * @param limit Maximum number of comments to return
     * @return Iterable of comments
     */
    public List<Comment> findByUserId(String userId, int limit) {
        logger.debug("Finding comments by user: {}, limit: {}", userId, limit);
        return commentCollection.find(
            Filters.eq("user_id", userId),
            new CollectionFindOptions().sort(Sort.descending("timestamp"))
            .limit(limit))
            .toList();
    }

    /**
     * Updates an existing comment document.
     *
     * @param comment The comment to update
     * @throws IllegalArgumentException if comment ID is null
     */
    public void update(Comment comment) {
        if (comment.getCommentId() == null) {
            logger.error("Attempted to update comment with null ID");
            throw new IllegalArgumentException("Comment ID cannot be null for update");
        }
        logger.debug("Updating comment with ID: {}", comment.getCommentId());
        commentCollection.replaceOne(Filters.eq("comment_id", comment.getCommentId()), comment);
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param commentId The ID of the comment to delete
     */
    public void deleteByCommentId(String commentId) {
        logger.debug("Deleting comment with ID: {}", commentId);
        commentCollection.deleteOne(Filters.eq("comment_id", commentId));
    }
}