package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Request DTO for semantic summarization operations.
 * This performs semantic search and then summarizes the results in one operation.
 */
public class SemanticSummarizationRequest {
    
    @JsonProperty("query")
    @NotBlank(message = "Query is required")
    @Size(max = 1000, message = "Query must be less than 1000 characters")
    private String query;
    
    @JsonProperty("indexName")
    @NotBlank(message = "Index name is required")
    @Size(max = 100, message = "Index name must be less than 100 characters")
    private String indexName;
    
    @JsonProperty("maxResults")
    @Min(value = 1, message = "Maximum results must be at least 1")
    @Max(value = 50, message = "Maximum results cannot exceed 50")
    private Integer maxResults = 10;
    
    @JsonProperty("minScore")
    @Min(value = 0, message = "Minimum score must be non-negative")
    @Max(value = 1, message = "Minimum score cannot exceed 1.0")
    private Double minScore = 0.0;
    
    @JsonProperty("searchType")
    private SearchType searchType = SearchType.VECTOR;
    
    @JsonProperty("maxSummaryLength")
    private Integer maxSummaryLength;
    
    @JsonProperty("includeSourceReferences")
    private Boolean includeSourceReferences = true;
    
    @JsonProperty("includeSearchResults")
    private Boolean includeSearchResults = false;
    
    @JsonProperty("customPrompt")
    @Size(max = 500, message = "Custom prompt must be less than 500 characters")
    private String customPrompt;
    
    public enum SearchType {
        VECTOR,
        HYBRID
    }
    
    public SemanticSummarizationRequest() {}
    
    public SemanticSummarizationRequest(String query, String indexName) {
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
    
    public Integer getMaxResults() {
        return maxResults;
    }
    
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }
    
    public Double getMinScore() {
        return minScore;
    }
    
    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }
    
    public SearchType getSearchType() {
        return searchType;
    }
    
    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
    
    public Integer getMaxSummaryLength() {
        return maxSummaryLength;
    }
    
    public void setMaxSummaryLength(Integer maxSummaryLength) {
        this.maxSummaryLength = maxSummaryLength;
    }
    
    public Boolean getIncludeSourceReferences() {
        return includeSourceReferences;
    }
    
    public void setIncludeSourceReferences(Boolean includeSourceReferences) {
        this.includeSourceReferences = includeSourceReferences;
    }
    
    public Boolean getIncludeSearchResults() {
        return includeSearchResults;
    }
    
    public void setIncludeSearchResults(Boolean includeSearchResults) {
        this.includeSearchResults = includeSearchResults;
    }
    
    public String getCustomPrompt() {
        return customPrompt;
    }
    
    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }
}
