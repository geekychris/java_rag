package com.example.ragservice.dto;

import com.example.ragservice.model.SearchResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for summarization operations.
 * Contains the original query and search results to be summarized.
 */
public class SummarizationRequest {
    
    @JsonProperty("query")
    @NotBlank(message = "Query is required")
    @Size(max = 1000, message = "Query must be less than 1000 characters")
    private String query;
    
    @JsonProperty("searchResults")
    @NotEmpty(message = "Search results are required")
    @Size(max = 50, message = "Maximum 50 search results allowed for summarization")
    private List<SearchResult> searchResults;
    
    @JsonProperty("maxSummaryLength")
    private Integer maxSummaryLength;
    
    @JsonProperty("includeSourceReferences")
    private Boolean includeSourceReferences = true;
    
    @JsonProperty("customPrompt")
    @Size(max = 500, message = "Custom prompt must be less than 500 characters")
    private String customPrompt;
    
    public SummarizationRequest() {}
    
    public SummarizationRequest(String query, List<SearchResult> searchResults) {
        this.query = query;
        this.searchResults = searchResults;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public List<SearchResult> getSearchResults() {
        return searchResults;
    }
    
    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
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
    
    public String getCustomPrompt() {
        return customPrompt;
    }
    
    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }
}
