package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for summarization operations.
 */
public class SummarizationResponse {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("sourceCount")
    private int sourceCount;
    
    @JsonProperty("sourceReferences")
    private List<String> sourceReferences;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    public SummarizationResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public SummarizationResponse(String query, String summary, boolean success, int sourceCount) {
        this();
        this.query = query;
        this.summary = summary;
        this.success = success;
        this.sourceCount = sourceCount;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
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
    
    public int getSourceCount() {
        return sourceCount;
    }
    
    public void setSourceCount(int sourceCount) {
        this.sourceCount = sourceCount;
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
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
