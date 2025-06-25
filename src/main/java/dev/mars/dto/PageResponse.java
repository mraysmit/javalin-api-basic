package dev.mars.dto;

import java.util.List;

/**
 * Response object for paginated data.
 */
public class PageResponse<T> {
    private List<T> content;
    private PageMetadata metadata;

    public PageResponse() {}

    public PageResponse(List<T> content, PageMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PageMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Creates a PageResponse from content and pagination parameters.
     */
    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        PageMetadata metadata = new PageMetadata(
            pageRequest.getPage(),
            pageRequest.getSize(),
            totalElements,
            content.size()
        );
        return new PageResponse<>(content, metadata);
    }

    /**
     * Metadata about the page.
     */
    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int numberOfElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;

        public PageMetadata() {}

        public PageMetadata(int page, int size, long totalElements, int numberOfElements) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.numberOfElements = numberOfElements;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
            this.first = page == 0;
            this.last = page >= totalPages - 1;
            this.hasNext = page < totalPages - 1;
            this.hasPrevious = page > 0;
        }

        // Getters and setters
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getNumberOfElements() { return numberOfElements; }
        public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }

        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

        public boolean isHasPrevious() { return hasPrevious; }
        public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    }
}
