package com.killrvideo.dto;

import java.util.List;

public class SearchVideosResponse {
    private List<VideoResponse> data;
    private Pagination pagination;
    
    public SearchVideosResponse(List<VideoResponse> data) {
        this.data = data;
        this.pagination = new Pagination(1, data.size(), data.size());
    }

    public List<VideoResponse> getData() {
        return data;
    }

    public void setData(List<VideoResponse> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}

class Pagination {
    private int pages;
    private int pageSize;
    private int totalItems;

    public Pagination(int pages, int pageSize, int totalItems) {
        this.pages = pages;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public int getPages() {
        return pages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }
}
