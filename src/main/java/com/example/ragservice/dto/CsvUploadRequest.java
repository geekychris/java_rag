package com.example.ragservice.dto;

import jakarta.validation.constraints.NotBlank;

public class CsvUploadRequest {
    
    @NotBlank(message = "CSV content cannot be blank")
    private String csvContent;
    
    @NotBlank(message = "Index name cannot be blank")
    private String indexName;
    
    private String contentColumnName = "content";
    
    private String source;

    public CsvUploadRequest() {}

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
}
