package com.example.ragservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(embeddingClient);
    }

    @Test
    void testGenerateEmbedding_Success() {
        // Given
        String testText = "This is a test document";
        List<Double> expectedEmbedding = Arrays.asList(0.1, 0.2, 0.3, 0.4);
        
        Embedding mockEmbedding = new Embedding(expectedEmbedding, 0);
        EmbeddingResponse mockResponse = new EmbeddingResponse(List.of(mockEmbedding));
        
        when(embeddingClient.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

        // When
        List<Double> result = embeddingService.generateEmbedding(testText);

        // Then
        assertNotNull(result);
        assertEquals(expectedEmbedding, result);
        assertEquals(4, result.size());
    }

    @Test
    void testGenerateEmbeddings_MultipleTexts() {
        // Given
        List<String> testTexts = Arrays.asList("Text 1", "Text 2");
        List<Double> embedding1 = Arrays.asList(0.1, 0.2);
        List<Double> embedding2 = Arrays.asList(0.3, 0.4);
        
        List<Embedding> mockEmbeddings = Arrays.asList(
            new Embedding(embedding1, 0),
            new Embedding(embedding2, 1)
        );
        EmbeddingResponse mockResponse = new EmbeddingResponse(mockEmbeddings);
        
        when(embeddingClient.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

        // When
        List<List<Double>> results = embeddingService.generateEmbeddings(testTexts);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(embedding1, results.get(0));
        assertEquals(embedding2, results.get(1));
    }

    @Test
    void testCalculateCosineSimilarity_IdenticalVectors() {
        // Given
        List<Double> vector1 = Arrays.asList(1.0, 0.0, 0.0);
        List<Double> vector2 = Arrays.asList(1.0, 0.0, 0.0);

        // When
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Then
        assertEquals(1.0, similarity, 0.001);
    }

    @Test
    void testCalculateCosineSimilarity_OrthogonalVectors() {
        // Given
        List<Double> vector1 = Arrays.asList(1.0, 0.0);
        List<Double> vector2 = Arrays.asList(0.0, 1.0);

        // When
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Then
        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    void testCalculateCosineSimilarity_DifferentDimensions() {
        // Given
        List<Double> vector1 = Arrays.asList(1.0, 0.0);
        List<Double> vector2 = Arrays.asList(1.0, 0.0, 0.0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            embeddingService.calculateCosineSimilarity(vector1, vector2)
        );
    }

    @Test
    void testCalculateCosineSimilarity_ZeroVector() {
        // Given
        List<Double> vector1 = Arrays.asList(0.0, 0.0);
        List<Double> vector2 = Arrays.asList(1.0, 0.0);

        // When
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Then
        assertEquals(0.0, similarity, 0.001);
    }
}
