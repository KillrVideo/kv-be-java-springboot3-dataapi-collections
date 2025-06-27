package com.killrvideo.dto;

public class YoutubeMetadata {
    private String title;
    private String description;
    private String thumbnailUrl;
    private String[] tags;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
