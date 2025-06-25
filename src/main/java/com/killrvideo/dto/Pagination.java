package com.killrvideo.dto;

public class Pagination {
    // "currentPage":1,"pageSize":10,"totalItems":0,"totalPages":0
    private int totalPages;
    private int pageSize;
    private int totalItems;
    private int currentPage;

    public Pagination(int currentPage, int totalPages, int pageSize, int totalItems) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public Pagination(int currentPage, int pageSize, int totalItems) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;

        if (pageSize > 0) {
            this.totalPages = totalItems / pageSize;
        } else {
            this.totalPages = 1;
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }
}
