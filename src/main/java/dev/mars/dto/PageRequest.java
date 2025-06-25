package dev.mars.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request object for pagination parameters.
 */
public class PageRequest {
    
    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int size = 20;
    
    private String sortBy;
    private SortDirection sortDirection = SortDirection.ASC;

    public PageRequest() {}

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageRequest(int page, int size, String sortBy, SortDirection sortDirection) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public int getOffset() {
        return page * size;
    }

    public enum SortDirection {
        ASC, DESC
    }

    @Override
    public String toString() {
        return String.format("PageRequest{page=%d, size=%d, sortBy='%s', sortDirection=%s}", 
            page, size, sortBy, sortDirection);
    }
}
