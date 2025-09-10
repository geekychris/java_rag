package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class DirectoryScanRequest {
    
    @NotBlank(message = "Directory path is required")
    @JsonProperty("directory_path")
    private String directoryPath;
    
    @JsonProperty("output_csv_path")
    private String outputCsvPath;
    
    @JsonProperty("supported_extensions")
    private List<String> supportedExtensions = List.of("pdf", "txt", "docx", "doc", "rtf", "html", "xml");
    
    @JsonProperty("recursive")
    private boolean recursive = true;
    
    @JsonProperty("max_files")
    private Integer maxFiles;
    
    public DirectoryScanRequest() {}
    
    public DirectoryScanRequest(String directoryPath, String outputCsvPath) {
        this.directoryPath = directoryPath;
        this.outputCsvPath = outputCsvPath;
    }
    
    // Getters and setters
    public String getDirectoryPath() {
        return directoryPath;
    }
    
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
    
    public String getOutputCsvPath() {
        return outputCsvPath;
    }
    
    public void setOutputCsvPath(String outputCsvPath) {
        this.outputCsvPath = outputCsvPath;
    }
    
    public List<String> getSupportedExtensions() {
        return supportedExtensions;
    }
    
    public void setSupportedExtensions(List<String> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
    
    public Integer getMaxFiles() {
        return maxFiles;
    }
    
    public void setMaxFiles(Integer maxFiles) {
        this.maxFiles = maxFiles;
    }
}
