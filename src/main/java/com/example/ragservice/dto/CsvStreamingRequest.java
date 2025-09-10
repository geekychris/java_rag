package com.example.ragservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class CsvStreamingRequest {
    
    @NotBlank(message = "CSV file path is required")
    @JsonProperty("csv_file_path")
    private String csvFilePath;
    
    @JsonProperty("batch_size")
    @Min(value = 1, message = "Batch size must be at least 1")
    private int batchSize = 100;
    
    @JsonProperty("text_column")
    private String textColumn = "text";
    
    @JsonProperty("metadata_columns")
    private String[] metadataColumns;
    
    @JsonProperty("skip_header")
    private boolean skipHeader = true;
    
    @JsonProperty("delimiter")
    private char delimiter = ',';
    
    @JsonProperty("quote_character")
    private char quoteCharacter = '"';
    
    @JsonProperty("escape_character")
    private char escapeCharacter = '\\';
    
    @JsonProperty("index_name")
    private String indexName;
    
    public CsvStreamingRequest() {}
    
    public CsvStreamingRequest(String csvFilePath, String indexName) {
        this.csvFilePath = csvFilePath;
        this.indexName = indexName;
    }
    
    // Getters and setters
    public String getCsvFilePath() {
        return csvFilePath;
    }
    
    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public String getTextColumn() {
        return textColumn;
    }
    
    public void setTextColumn(String textColumn) {
        this.textColumn = textColumn;
    }
    
    public String[] getMetadataColumns() {
        return metadataColumns;
    }
    
    public void setMetadataColumns(String[] metadataColumns) {
        this.metadataColumns = metadataColumns;
    }
    
    public boolean isSkipHeader() {
        return skipHeader;
    }
    
    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }
    
    public char getDelimiter() {
        return delimiter;
    }
    
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }
    
    public char getQuoteCharacter() {
        return quoteCharacter;
    }
    
    public void setQuoteCharacter(char quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }
    
    public char getEscapeCharacter() {
        return escapeCharacter;
    }
    
    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
