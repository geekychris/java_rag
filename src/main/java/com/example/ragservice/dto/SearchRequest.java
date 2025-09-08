package com.example.ragservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SearchRequest {
    
    @NotBlank(message = "Query cannot be blank")
    private String query;
    
    @NotBlank(message = "Index name cannot be blank")
    private String indexName;
    
    @Min(value = 1, message = "Size must be at least 1")
    private int size = 10;
    
    private double minScore = 0.0;

    public SearchRequest() {}

    public SearchRequest(String query, String indexName, int size, double minScore) {
        this.query = query;
        this.indexName = indexName;
        this.size = size;
        this.minScore = minScore;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }
}
