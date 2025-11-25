package com.killrvideo.dao;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
//import com.datastax.astra.client.collections.commands.Update;

import com.killrvideo.dto.Video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;

@Repository
public class VideoDao {
    private static final Logger logger = LoggerFactory.getLogger(VideoDao.class);
    private final Collection<Video> videoCollection;
    //private final Collection<Document> videoDocCollection;

    @Autowired
    public VideoDao(Database killrVideoDatabase) {
        this.videoCollection = killrVideoDatabase.getCollection("videos", Video.class);
        //this.videoDocCollection = killrVideoDatabase.getCollection("videos");
        logger.info("Initialized VideoDao with 'videos' collection");
    }

    /**
     * Saves a new video document to the database.
     * If the videoId is not set, generates a new UUID.
     *
     * @param video The video to save
     * @return The saved video
     */
    public Video save(Video video) {
        if (video.getVideoid() == null) {
            video.setVideoid(UUID.randomUUID().toString());
            logger.debug("Generated new video ID: {}", video.getVideoid());
        }
        videoCollection.insertOne(video);
        logger.debug("Saved video with ID: {}", video.getVideoid());
        return video;
    }

    /**
     * Finds a video by its database ID.
     *
     * @param database ID of the video to find
     * @return Optional containing the video if found, empty otherwise
     */
    public Optional<Video> findById(String id) {
        logger.debug("Finding video by databaseID: {}", id);
        Optional<Video> video = videoCollection.findById(id);
        return video;
    }

        /**
     * Finds a video by its video ID.
     *
     * @param videoId of the video to find
     * @return Optional containing the video if found, empty otherwise
     */
    public Optional<Video> findByVideoId(String videoId, boolean includeVector) {
        logger.debug("Finding video by video ID: {}", videoId);

        if (includeVector) {
            CollectionFindOneOptions options = new CollectionFindOneOptions();
            options.projection(new Projection("$vector", true));
            Optional<Video> video = videoCollection.findOne(Filters.eq("videoid", videoId), options);

            return video;
        } else {
            Optional<Video> video = videoCollection.findOne(Filters.eq("videoid", videoId));
            return video;
        }
    }

    /**
     * Finds the latest videos, sorted by added date in descending order.
     *
     * @param limit Maximum number of videos to return
     * @return Iterable of videos
     */
    public List<Video> findLatest(int limit) {
        logger.debug("Finding latest {} videos", limit);
        return videoCollection.find(new CollectionFindOptions().sort(Sort.descending("added_date"))
            .limit(limit))
            .toList();
    }

    public List<Video> findTrending(int days, int limit) {

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        Filter filter = Filters.gte("last_viewed", cutoff);
        return videoCollection.find(
            filter,
            new CollectionFindOptions().sort(Sort.descending("last_viewed"))
                .limit(limit))
                .toList();
    }

    /**
     * Finds videos by user ID, sorted by added date in descending order.
     *
     * @param userId The ID of the user
     * @param limit Maximum number of videos to return
     * @return Iterable of videos
     */
    public List<Video> findByUserId(String userId, int limit) {
        logger.debug("Finding videos for user: {}, limit: {}", userId, limit);
        return videoCollection.find(
            Filters.eq("userid", userId),
            new CollectionFindOptions().sort(Sort.descending("added_date"))
            .limit(limit))
            .toList();
    }

    /**
     * Updates an existing video document.
     *
     * @param video The video to update
     * @throws IllegalArgumentException if video ID is null
     */
    public void update(Video video) {
    	String videoid = video.getVideoid();
        logger.debug("Updating video with ID: {}", videoid);
        videoCollection.replaceOne(Filters.eq("videoid", videoid), video);
    }

//    public void updateViews(String videoId, long views, Instant lastViewed) {
//        logger.debug("Updating views for video with ID: {}", videoId);
//        videoCollection.updateOne(Filters.eq("videoid", videoId),
//            new Update()
//                .set("views", views)
//                .set("last_viewed", lastViewed));
//    }

    /**
     * Deletes a video by its ID.
     *
     * @param videoId The ID of the video to delete
     */
    public void deleteById(String videoId) {
        logger.debug("Deleting video with ID: {}", videoId);
        videoCollection.deleteOne(Filters.eq("videoid", videoId));
    }

    /**
     * Finds videos by a specific tag.
     * Assumes 'tags' field in Astra is an array/list where direct equality match can find if the tag exists.
     * @param tag The tag to search for.
     * @param limit Max number of videos to return.
     * @return FindIterable of videos.
     */
    public List<Video> findByTag(String tag, int limit) {
        if (tag == null || tag.trim().isEmpty()) {
            return new ArrayList<>();
        }
        logger.debug("Finding videos with tag: {}, limit: {}", tag, limit);
        return videoCollection.find(
            Filters.eq("tags", tag),
            new CollectionFindOptions().sort(Sort.descending("added_date"))
            .limit(limit))
            .toList();
    }

    /**
     * Finds videos based on vector similarity.
     * Assumes the collection is indexed for vector search on a field (e.g., mapped from 'vector' POJO field).
     * @param vector The query vector.
     * @param limit Max number of similar videos to return.
     * @return FindIterable of similar videos.
     */
    public List<Video> findByVector(float[] vector, int limit) {
        if (vector == null) {
            logger.error("Attempted vector search with null vector");
            throw new IllegalArgumentException("Query vector cannot be null.");
        }
        logger.debug("Finding similar videos with vector search, limit: {}", limit);

        return videoCollection.find(
            null,
            new CollectionFindOptions()
                .sort(Sort.vector(vector))
                .limit(limit)
            )
            .toList();
    }

    /**
     * Search videos by text query using vector similarity.
     * @param searchVector The search vector.
     * @param limit Max number of videos to return.
     * @return Optional containing FindIterable of matching videos.
     */
    public Optional<List<Video>> searchVideos(float[] searchVector, int limit) {
        if (searchVector == null) {
            logger.warn("Attempted to search with null search vector");
            return Optional.empty();
        }
        logger.debug("Searching videos with vector search, limit: {}", limit);
        
        List<Video> results = videoCollection.find(
            null,
            new CollectionFindOptions()
                .sort(Sort.vector(searchVector))
                .limit(limit))
            .toList();
        return Optional.of(results);
    }

    /**
     * Get tag suggestions based on a query.
     * @param query The query to search for in tags.
     * @param limit Max number of tag suggestions to return.
     * @return List of unique tag suggestions.
     */
    public List<String> suggestTags(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Attempted to suggest tags with null or empty query");
            return List.of();
        }
        logger.debug("Suggesting tags with query: {}, limit: {}", query, limit);
        
        // Get all videos and extract tags that match the query
        return videoCollection.find((Filter) null)
            .stream()
            .filter(video -> video.getTags() != null)
            .flatMap(video -> video.getTags().stream())
            .filter(tag -> tag.toLowerCase().contains(query.toLowerCase()))
            .distinct()
            .limit(limit)
            .toList();
    }
    
    private Video getVideoFromDoc(Document doc) {
    	Video video = new Video();
    	
    	video.setAddedDate(doc.getInstant("added_date"));
    	video.setDescription(doc.getString("description"));
    	video.setName(doc.getString("name"));
    	video.setUserid(doc.getString("userid"));
    	video.setVideoid(doc.getString("videoid"));
    	video.setLocation(doc.getString("location"));
    	
    	Optional<DataAPIVector> docVector = doc.getVector();
    	
    	//if (docVector.isPresent()) {
    	//	//var vector = docVector.get()[0].getValue("$binary");
    	//	video.setVector(docVector.get());
    	//}

    	return video;
    }
} 