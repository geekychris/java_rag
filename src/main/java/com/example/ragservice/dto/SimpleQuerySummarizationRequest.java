package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/**
 * Simple request DTO for one-step query summarization.
 * This covers the most common use case: provide a query and get a summary.
 */
public class SimpleQuerySummarizationRequest {
    
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
    private Integer maxResults; // Optional, defaults to 10
    
    @JsonProperty("minScore")
    @Min(value = 0, message = "Minimum score must be non-negative")
    private Double minScore; // Optional, defaults to 0.01
    
    @JsonProperty("maxSummaryLength")
    private Integer maxSummaryLength; // Optional
    
    @JsonProperty("customPrompt")
    @Size(max = 500, message = "Custom prompt must be less than 500 characters")
    private String customPrompt; // Optional
    
    public SimpleQuerySummarizationRequest() {}
    
    public SimpleQuerySummarizationRequest(String query, String indexName) {
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
    
    public Integer getMaxSummaryLength() {
        return maxSummaryLength;
    }
    
    public void setMaxSummaryLength(Integer maxSummaryLength) {
        this.maxSummaryLength = maxSummaryLength;
    }
    
    public String getCustomPrompt() {
        return customPrompt;
    }
    
    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }
}
