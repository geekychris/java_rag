package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Enhanced search response DTO that can provide different levels of detail
 * based on what the client needs - either full document results or lightweight
 * metadata-only results.
 */
public class EnhancedSearchResponse {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("indexName")
    private String indexName;
    
    @JsonProperty("success")
    private boolean success = true;
    
    @JsonProperty("totalResults")
    private int totalResults;
    
    @JsonProperty("searchType")
    private String searchType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("error")
    private String error;
    
    // Different result formats based on response type requested
    @JsonProperty("fullResults")
    private List<DetailedSearchResult> fullResults;
    
    @JsonProperty("lightweightResults")
    private List<LightweightSearchResult> lightweightResults;
    
    @JsonProperty("documentSummaries")
    private List<DocumentSummary> documentSummaries;
    
    public enum ResponseType {
        FULL,           // Complete document content + metadata
        LIGHTWEIGHT,    // Document ID + metadata + score only
        SUMMARY         // Document ID + summary snippet + metadata
    }
    
    public static class DetailedSearchResult {
        @JsonProperty("documentId")
        private String documentId;
        
        @JsonProperty("content")
        private String content;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("score")
        private double score;
        
        @JsonProperty("embedding")
        private List<Double> embedding; // Optional - only if requested
        
        // Constructors and getters/setters
        public DetailedSearchResult() {}
        
        public DetailedSearchResult(String documentId, String content, Map<String, Object> metadata, 
                                   String source, double score) {
            this.documentId = documentId;
            this.content = content;
            this.metadata = metadata;
            this.source = source;
            this.score = score;
        }
        
        // Getters and setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        
        public List<Double> getEmbedding() { return embedding; }
        public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    }
    
    public static class LightweightSearchResult {
        @JsonProperty("documentId")
        private String documentId;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("score")
        private double score;
        
        public LightweightSearchResult() {}
        
        public LightweightSearchResult(String documentId, Map<String, Object> metadata, 
                                     String source, double score) {
            this.documentId = documentId;
            this.metadata = metadata;
            this.source = source;
            this.score = score;
        }
        
        // Getters and setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
    
    public static class DocumentSummary {
        @JsonProperty("documentId")
        private String documentId;
        
        @JsonProperty("snippet")
        private String snippet; // First N characters of content
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("score")
        private double score;
        
        @JsonProperty("contentLength")
        private int contentLength;
        
        public DocumentSummary() {}
        
        public DocumentSummary(String documentId, String snippet, Map<String, Object> metadata,
                              String source, double score, int contentLength) {
            this.documentId = documentId;
            this.snippet = snippet;
            this.metadata = metadata;
            this.source = source;
            this.score = score;
            this.contentLength = contentLength;
        }
        
        // Getters and setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        
        public int getContentLength() { return contentLength; }
        public void setContentLength(int contentLength) { this.contentLength = contentLength; }
    }
    
    public EnhancedSearchResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public EnhancedSearchResponse(String query, String indexName) {
        this();
        this.query = query;
        this.indexName = indexName;
    }
    
    // Main getters and setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
    
    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public List<DetailedSearchResult> getFullResults() { return fullResults; }
    public void setFullResults(List<DetailedSearchResult> fullResults) { 
        this.fullResults = fullResults;
        this.totalResults = fullResults != null ? fullResults.size() : 0;
    }
    
    public List<LightweightSearchResult> getLightweightResults() { return lightweightResults; }
    public void setLightweightResults(List<LightweightSearchResult> lightweightResults) { 
        this.lightweightResults = lightweightResults;
        this.totalResults = lightweightResults != null ? lightweightResults.size() : 0;
    }
    
    public List<DocumentSummary> getDocumentSummaries() { return documentSummaries; }
    public void setDocumentSummaries(List<DocumentSummary> documentSummaries) { 
        this.documentSummaries = documentSummaries;
        this.totalResults = documentSummaries != null ? documentSummaries.size() : 0;
    }
}
