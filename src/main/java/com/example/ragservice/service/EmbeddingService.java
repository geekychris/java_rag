package com.example.ragservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    
    private final EmbeddingClient embeddingClient;
    
    @Autowired
    public EmbeddingService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }
    
    /**
     * Generate embeddings for a single text
     * @param text The text to generate embeddings for
     * @return List of embedding values
     */
    public List<Double> generateEmbedding(String text) {
        try {
            logger.debug("Generating embedding for text: {}", text.substring(0, Math.min(100, text.length())));
            
            EmbeddingResponse response = embeddingClient.call(
                new EmbeddingRequest(List.of(text), null)
            );
            
            if (response.getResults().isEmpty()) {
                throw new RuntimeException("No embedding generated for text");
            }
            
            List<Double> embedding = response.getResults().get(0).getOutput();
            logger.debug("Generated embedding with {} dimensions", embedding.size());
            
            return embedding;
            
        } catch (Exception e) {
            logger.error("Failed to generate embedding for text", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }
    
    /**
     * Generate embeddings for multiple texts
     * @param texts List of texts to generate embeddings for
     * @return List of embedding lists
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        try {
            logger.debug("Generating embeddings for {} texts", texts.size());
            
            EmbeddingResponse response = embeddingClient.call(
                new EmbeddingRequest(texts, null)
            );
            
            List<List<Double>> embeddings = response.getResults().stream()
                .map(result -> result.getOutput())
                .collect(Collectors.toList());
            
            logger.debug("Generated {} embeddings", embeddings.size());
            
            return embeddings;
            
        } catch (Exception e) {
            logger.error("Failed to generate embeddings for {} texts", texts.size(), e);
            throw new RuntimeException("Failed to generate embeddings", e);
        }
    }
    
    /**
     * Calculate cosine similarity between two embedding vectors
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Cosine similarity score between 0 and 1
     */
    public double calculateCosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1.size() != embedding2.size()) {
            throw new IllegalArgumentException("Embedding vectors must have the same dimension");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += embedding1.get(i) * embedding1.get(i);
            norm2 += embedding2.get(i) * embedding2.get(i);
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
