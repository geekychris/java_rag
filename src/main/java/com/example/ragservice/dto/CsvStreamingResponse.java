package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class CsvStreamingResponse {
    
    @JsonProperty("stream_id")
    private String streamId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("records_processed")
    private long recordsProcessed;
    
    @JsonProperty("records_indexed")
    private long recordsIndexed;
    
    @JsonProperty("records_failed")
    private long recordsFailed;
    
    @JsonProperty("total_records")
    private long totalRecords;
    
    @JsonProperty("batch_count")
    private long batchCount;
    
    @JsonProperty("current_batch")
    private long currentBatch;
    
    @JsonProperty("progress_percentage")
    private double progressPercentage;
    
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    @JsonProperty("end_time")
    private LocalDateTime endTime;
    
    @JsonProperty("duration_ms")
    private long durationMs;
    
    @JsonProperty("processing_rate_per_second")
    private double processingRatePerSecond;
    
    @JsonProperty("index_name")
    private String indexName;
    
    @JsonProperty("errors")
    private List<String> errors;
    
    @JsonProperty("warnings")
    private List<String> warnings;
    
    public CsvStreamingResponse() {}
    
    public CsvStreamingResponse(String streamId, String status, String indexName) {
        this.streamId = streamId;
        this.status = status;
        this.indexName = indexName;
        this.startTime = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getStreamId() {
        return streamId;
    }
    
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getRecordsProcessed() {
        return recordsProcessed;
    }
    
    public void setRecordsProcessed(long recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
    
    public long getRecordsIndexed() {
        return recordsIndexed;
    }
    
    public void setRecordsIndexed(long recordsIndexed) {
        this.recordsIndexed = recordsIndexed;
    }
    
    public long getRecordsFailed() {
        return recordsFailed;
    }
    
    public void setRecordsFailed(long recordsFailed) {
        this.recordsFailed = recordsFailed;
    }
    
    public long getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public long getBatchCount() {
        return batchCount;
    }
    
    public void setBatchCount(long batchCount) {
        this.batchCount = batchCount;
    }
    
    public long getCurrentBatch() {
        return currentBatch;
    }
    
    public void setCurrentBatch(long currentBatch) {
        this.currentBatch = currentBatch;
    }
    
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    public double getProcessingRatePerSecond() {
        return processingRatePerSecond;
    }
    
    public void setProcessingRatePerSecond(double processingRatePerSecond) {
        this.processingRatePerSecond = processingRatePerSecond;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
