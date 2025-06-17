package com.killrvideo.dto;

import java.util.List;

public class LatestVideosResponse {

    private List<VideoResponse> data;
    
    public LatestVideosResponse(List<VideoResponse> data) {
        this.data = data;
    }

    public List<VideoResponse> getData() {
        return data;
    }

    public void setData(List<VideoResponse> data) {
        this.data = data;
    }
}
