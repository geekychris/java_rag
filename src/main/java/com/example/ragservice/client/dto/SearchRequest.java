package com.example.ragservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for search operations in the RAG client.
 */
public class SearchRequest {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("indexName")
    private String indexName;
    
    @JsonProperty("maxResults")
    private int maxResults = 10;
    
    @JsonProperty("minScore")
    private double minScore = 0.0;
    
    @JsonProperty("searchType")
    private String searchType = "VECTOR"; // VECTOR, HYBRID
    
    public SearchRequest() {}
    
    public SearchRequest(String query, String indexName) {
        this.query = query;
        this.indexName = indexName;
    }
    
    public SearchRequest(String query, String indexName, int maxResults, double minScore) {
        this.query = query;
        this.indexName = indexName;
        this.maxResults = maxResults;
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
    
    public int getMaxResults() {
        return maxResults;
    }
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    public double getMinScore() {
        return minScore;
    }
    
    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }
    
    public String getSearchType() {
        return searchType;
    }
    
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String query;
        private String indexName;
        private int maxResults = 10;
        private double minScore = 0.0;
        private String searchType = "VECTOR";
        
        public Builder query(String query) {
            this.query = query;
            return this;
        }
        
        public Builder indexName(String indexName) {
            this.indexName = indexName;
            return this;
        }
        
        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }
        
        public Builder minScore(double minScore) {
            this.minScore = minScore;
            return this;
        }
        
        public Builder searchType(String searchType) {
            this.searchType = searchType;
            return this;
        }
        
        public Builder vectorSearch() {
            this.searchType = "VECTOR";
            return this;
        }
        
        public Builder hybridSearch() {
            this.searchType = "HYBRID";
            return this;
        }
        
        public SearchRequest build() {
            SearchRequest request = new SearchRequest();
            request.setQuery(query);
            request.setIndexName(indexName);
            request.setMaxResults(maxResults);
            request.setMinScore(minScore);
            request.setSearchType(searchType);
            return request;
        }
    }
}
