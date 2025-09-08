package com.example.ragservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for CSV upload operations in the RAG client.
 */
public class CsvUploadRequest {
    
    @JsonProperty("csvContent")
    private String csvContent;
    
    @JsonProperty("indexName")
    private String indexName;
    
    @JsonProperty("contentColumnName")
    private String contentColumnName = "content";
    
    @JsonProperty("source")
    private String source;
    
    public CsvUploadRequest() {}
    
    public CsvUploadRequest(String csvContent, String indexName) {
        this.csvContent = csvContent;
        this.indexName = indexName;
    }
    
    public CsvUploadRequest(String csvContent, String indexName, String contentColumnName, String source) {
        this.csvContent = csvContent;
        this.indexName = indexName;
        this.contentColumnName = contentColumnName;
        this.source = source;
    }
    
    public String getCsvContent() {
        return csvContent;
    }
    
    public void setCsvContent(String csvContent) {
        this.csvContent = csvContent;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public String getContentColumnName() {
        return contentColumnName;
    }
    
    public void setContentColumnName(String contentColumnName) {
        this.contentColumnName = contentColumnName;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String csvContent;
        private String indexName;
        private String contentColumnName = "content";
        private String source;
        
        public Builder csvContent(String csvContent) {
            this.csvContent = csvContent;
            return this;
        }
        
        public Builder indexName(String indexName) {
            this.indexName = indexName;
            return this;
        }
        
        public Builder contentColumnName(String contentColumnName) {
            this.contentColumnName = contentColumnName;
            return this;
        }
        
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        public CsvUploadRequest build() {
            CsvUploadRequest request = new CsvUploadRequest();
            request.setCsvContent(csvContent);
            request.setIndexName(indexName);
            request.setContentColumnName(contentColumnName);
            request.setSource(source);
            return request;
        }
    }
}
