package com.example.ragservice.client.config;

import java.time.Duration;

/**
 * Configuration class for the RAG client library.
 * Contains settings for base URL, timeouts, and other client options.
 */
public class RagClientConfig {
    
    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxRetries;
    private final Duration retryDelay;
    
    private RagClientConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelay = builder.retryDelay;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public Duration getConnectTimeout() {
        return connectTimeout;
    }
    
    public Duration getReadTimeout() {
        return readTimeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public Duration getRetryDelay() {
        return retryDelay;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static RagClientConfig defaultConfig() {
        return builder().build();
    }
    
    public static class Builder {
        private String baseUrl = "http://localhost:8080";
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(60);
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofSeconds(1);
        
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }
        
        public RagClientConfig build() {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Base URL cannot be null or empty");
            }
            return new RagClientConfig(this);
        }
    }
    
    @Override
    public String toString() {
        return "RagClientConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", maxRetries=" + maxRetries +
                ", retryDelay=" + retryDelay +
                '}';
    }
}
