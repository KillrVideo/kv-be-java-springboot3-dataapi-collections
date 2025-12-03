package com.killrvideo.dto;

public class VideoPlaybackStats {
	private String videoid;
	private int views;
	private int totalPlayTime;
	private int completeViews;
	private int uniqueViewers;
	
	public String getVideoid() {
		return videoid;
	}
	public void setVideoid(String videoid) {
		this.videoid = videoid;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public int getTotalPlayTime() {
		return totalPlayTime;
	}
	public void setTotalPlayTime(int totalPlayTime) {
		this.totalPlayTime = totalPlayTime;
	}
	public int getCompleteViews() {
		return completeViews;
	}
	public void setCompleteViews(int completeViews) {
		this.completeViews = completeViews;
	}
	public int getUniqueViewers() {
		return uniqueViewers;
	}
	public void setUniqueViewers(int uniqueViewers) {
		this.uniqueViewers = uniqueViewers;
	}
}
