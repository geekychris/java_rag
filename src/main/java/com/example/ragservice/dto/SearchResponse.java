package com.example.ragservice.dto;

import com.example.ragservice.model.SearchResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for search operations.
 */
public class SearchResponse {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("results")
    private List<SearchResult> results;
    
    @JsonProperty("totalResults")
    private int totalResults;
    
    @JsonProperty("success")
    private boolean success;
    
    public SearchResponse() {}
    
    public SearchResponse(String query, List<SearchResult> results, int totalResults, boolean success) {
        this.query = query;
        this.results = results;
        this.totalResults = totalResults;
        this.success = success;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public List<SearchResult> getResults() {
        return results;
    }
    
    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
