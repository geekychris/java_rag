package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for CSV upload operations.
 */
public class CsvUploadResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("documentsIndexed")
    private int documentsIndexed;
    
    @JsonProperty("indexName")
    private String indexName;
    
    @JsonProperty("csvHeaders")
    private List<String> csvHeaders;
    
    @JsonProperty("message")
    private String message;
    
    public CsvUploadResponse() {}
    
    public CsvUploadResponse(boolean success, int documentsIndexed, String indexName, List<String> csvHeaders) {
        this.success = success;
        this.documentsIndexed = documentsIndexed;
        this.indexName = indexName;
        this.csvHeaders = csvHeaders;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getDocumentsIndexed() {
        return documentsIndexed;
    }
    
    public void setDocumentsIndexed(int documentsIndexed) {
        this.documentsIndexed = documentsIndexed;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public List<String> getCsvHeaders() {
        return csvHeaders;
    }
    
    public void setCsvHeaders(List<String> csvHeaders) {
        this.csvHeaders = csvHeaders;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
