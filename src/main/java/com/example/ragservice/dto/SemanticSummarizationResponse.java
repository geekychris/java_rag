package com.example.ragservice.dto;

import com.example.ragservice.model.SearchResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for semantic summarization operations.
 * Contains both the search results and the generated summary.
 */
public class SemanticSummarizationResponse {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("indexName")
    private String indexName;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("searchResults")
    private List<SearchResult> searchResults;
    
    @JsonProperty("totalResults")
    private int totalResults;
    
    @JsonProperty("sourceReferences")
    private List<String> sourceReferences;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("searchType")
    private String searchType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("searchTimeMs")
    private Long searchTimeMs;
    
    @JsonProperty("summarizationTimeMs") 
    private Long summarizationTimeMs;
    
    @JsonProperty("totalProcessingTimeMs")
    private Long totalProcessingTimeMs;
    
    @JsonProperty("error")
    private String error;
    
    public SemanticSummarizationResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public SemanticSummarizationResponse(String query, String indexName) {
        this();
        this.query = query;
        this.indexName = indexName;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<SearchResult> getSearchResults() {
        return searchResults;
    }
    
    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
        this.totalResults = searchResults != null ? searchResults.size() : 0;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    public List<String> getSourceReferences() {
        return sourceReferences;
    }
    
    public void setSourceReferences(List<String> sourceReferences) {
        this.sourceReferences = sourceReferences;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getSearchType() {
        return searchType;
    }
    
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getSearchTimeMs() {
        return searchTimeMs;
    }
    
    public void setSearchTimeMs(Long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }
    
    public Long getSummarizationTimeMs() {
        return summarizationTimeMs;
    }
    
    public void setSummarizationTimeMs(Long summarizationTimeMs) {
        this.summarizationTimeMs = summarizationTimeMs;
    }
    
    public Long getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }
    
    public void setTotalProcessingTimeMs(Long totalProcessingTimeMs) {
        this.totalProcessingTimeMs = totalProcessingTimeMs;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}
