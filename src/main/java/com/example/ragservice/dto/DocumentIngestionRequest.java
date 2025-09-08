package com.example.ragservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class DocumentIngestionRequest {
    
    @NotBlank(message = "Content cannot be blank")
    private String content;
    
    private Map<String, Object> metadata;
    
    private String source;
    
    @NotBlank(message = "Index name cannot be blank")
    private String indexName;

    public DocumentIngestionRequest() {}

    public DocumentIngestionRequest(String content, Map<String, Object> metadata, String source, String indexName) {
        this.content = content;
        this.metadata = metadata;
        this.source = source;
        this.indexName = indexName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
