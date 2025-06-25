package com.killrvideo.dto;

import java.util.List;

public class CommentsDataResponse {
    
    private List<CommentResponse> data;
    private Pagination pagination;

    public CommentsDataResponse(List<CommentResponse> data) {
        this.data = data;
        this.pagination = new Pagination(1, data.size(), data.size());
    }

    public CommentsDataResponse(List<CommentResponse> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public CommentsDataResponse(List<CommentResponse> data, int pages, int pageSize, int totalItems) {
        this.data = data;
        this.pagination = new Pagination(pages, pageSize, totalItems);
    }

    public List<CommentResponse> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}