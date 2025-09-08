package com.example.ragservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Document {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("embedding")
    private List<Double> embedding;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("source")
    private String source;

    public Document() {
        this.timestamp = Instant.now();
    }

    public Document(String id, String content, Map<String, Object> metadata) {
        this();
        this.id = id;
        this.content = content;
        this.metadata = metadata;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
}
