package com.example.ragservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class CsvFileIngestionRequest {
    
    @NotBlank(message = "CSV file path cannot be blank")
    private String csvFilePath;
    
    @NotBlank(message = "Index name cannot be blank")
    private String indexName;
    
    private String contentColumnName = "text";
    
    private String docIdColumnName = "doc_id";
    
    private String source;
    
    @Min(value = 1, message = "Batch size must be at least 1")
    private Integer batchSize = 100;
    
    @Min(value = 1, message = "Max records must be at least 1")
    private Integer maxRecords;

    public CsvFileIngestionRequest() {}

    public CsvFileIngestionRequest(String csvFilePath, String indexName, String contentColumnName, String source) {
        this.csvFilePath = csvFilePath;
        this.indexName = indexName;
        this.contentColumnName = contentColumnName;
        this.source = source;
    }
    
    public CsvFileIngestionRequest(String csvFilePath, String indexName, String contentColumnName, String docIdColumnName, String source) {
        this.csvFilePath = csvFilePath;
        this.indexName = indexName;
        this.contentColumnName = contentColumnName;
        this.docIdColumnName = docIdColumnName;
        this.source = source;
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }

    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
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
    
    public String getDocIdColumnName() {
        return docIdColumnName;
    }
    
    public void setDocIdColumnName(String docIdColumnName) {
        this.docIdColumnName = docIdColumnName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    public Integer getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
    
    public Integer getMaxRecords() {
        return maxRecords;
    }
    
    public void setMaxRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
    }
}
