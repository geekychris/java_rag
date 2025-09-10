package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class DirectoryScanResponse {
    
    @JsonProperty("scan_id")
    private String scanId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("files_processed")
    private int filesProcessed;
    
    @JsonProperty("files_failed")
    private int filesFailed;
    
    @JsonProperty("total_files_found")
    private int totalFilesFound;
    
    @JsonProperty("csv_output_path")
    private String csvOutputPath;
    
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    @JsonProperty("end_time")
    private LocalDateTime endTime;
    
    @JsonProperty("duration_ms")
    private long durationMs;
    
    @JsonProperty("errors")
    private List<String> errors;
    
    @JsonProperty("processed_extensions")
    private List<String> processedExtensions;
    
    public DirectoryScanResponse() {}
    
    public DirectoryScanResponse(String scanId, String status) {
        this.scanId = scanId;
        this.status = status;
        this.startTime = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getScanId() {
        return scanId;
    }
    
    public void setScanId(String scanId) {
        this.scanId = scanId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getFilesProcessed() {
        return filesProcessed;
    }
    
    public void setFilesProcessed(int filesProcessed) {
        this.filesProcessed = filesProcessed;
    }
    
    public int getFilesFailed() {
        return filesFailed;
    }
    
    public void setFilesFailed(int filesFailed) {
        this.filesFailed = filesFailed;
    }
    
    public int getTotalFilesFound() {
        return totalFilesFound;
    }
    
    public void setTotalFilesFound(int totalFilesFound) {
        this.totalFilesFound = totalFilesFound;
    }
    
    public String getCsvOutputPath() {
        return csvOutputPath;
    }
    
    public void setCsvOutputPath(String csvOutputPath) {
        this.csvOutputPath = csvOutputPath;
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
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public List<String> getProcessedExtensions() {
        return processedExtensions;
    }
    
    public void setProcessedExtensions(List<String> processedExtensions) {
        this.processedExtensions = processedExtensions;
    }
}
