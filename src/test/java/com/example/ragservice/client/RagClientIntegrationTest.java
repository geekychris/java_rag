package com.example.ragservice.client;

import com.example.ragservice.client.config.RagClientConfig;
import com.example.ragservice.client.dto.SearchRequest;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.client.exception.RagClientException;
import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.model.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the RAG client.
 * These tests require a running RAG service instance.
 * 
 * Run with: mvn test -Dintegration.test.enabled=true -Drag.service.url=http://localhost:8080
 */
@EnabledIfSystemProperty(named = "integration.test.enabled", matches = "true")
class RagClientIntegrationTest {
    
    private RagClient ragClient;
    private final String testIndexName = "integration-test-index";
    
    @BeforeEach
    void setUp() {
        String baseUrl = System.getProperty("rag.service.url", "http://localhost:8080");
        
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl(baseUrl)
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .build();
        
        ragClient = new RagClientImpl(config);
    }
    
    @Test
    void testHealthCheck() {
        assertTrue(ragClient.isHealthy(), "RAG service should be healthy");
    }
    
    @Test
    void testGetServiceInfo() throws RagClientException {
        String info = ragClient.getServiceInfo();
        assertNotNull(info);
        assertFalse(info.trim().isEmpty());
        System.out.println("Service Info: " + info);
    }
    
    @Test
    void testCsvUploadAndSearch() throws RagClientException {
        // Test CSV content with technology articles
        String csvContent = "title,content,category,author\n" +
                "Machine Learning Basics,\"Machine learning is a method of data analysis that automates analytical model building. It is a branch of artificial intelligence based on the idea that systems can learn from data, identify patterns and make decisions with minimal human intervention.\",Technology,John Smith\n" +
                "Deep Learning Introduction,\"Deep learning is part of a broader family of machine learning methods based on artificial neural networks. Learning can be supervised, semi-supervised or unsupervised.\",Technology,Jane Doe\n" +
                "Natural Language Processing,\"Natural Language Processing (NLP) is a subfield of linguistics, computer science, and artificial intelligence concerned with the interactions between computers and human language.\",Technology,Bob Johnson";
        
        // Upload CSV
        CsvUploadRequest uploadRequest = CsvUploadRequest.builder()
                .csvContent(csvContent)
                .indexName(testIndexName)
                .contentColumnName("content")
                .source("integration-test")
                .build();
        
        CsvUploadResponse uploadResponse = ragClient.uploadCsv(uploadRequest);
        
        assertNotNull(uploadResponse);
        assertTrue(uploadResponse.isSuccess());
        assertEquals(3, uploadResponse.getDocumentsIndexed());
        assertEquals(testIndexName, uploadResponse.getIndexName());
        assertNotNull(uploadResponse.getCsvHeaders());
        assertTrue(uploadResponse.getCsvHeaders().contains("title"));
        assertTrue(uploadResponse.getCsvHeaders().contains("content"));
        
        System.out.println("Successfully uploaded " + uploadResponse.getDocumentsIndexed() + " documents");
        
        // Wait a bit for indexing to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Test search
        SearchRequest searchRequest = SearchRequest.builder()
                .query("machine learning")
                .indexName(testIndexName)
                .maxResults(5)
                .minScore(0.1)
                .build();
        
        SearchResponse searchResponse = ragClient.search(searchRequest);
        
        assertNotNull(searchResponse);
        assertTrue(searchResponse.isSuccess());
        assertEquals("machine learning", searchResponse.getQuery());
        assertTrue(searchResponse.getTotalResults() > 0);
        assertNotNull(searchResponse.getResults());
        assertFalse(searchResponse.getResults().isEmpty());
        
        // Verify search results contain expected content
        boolean foundRelevantResult = searchResponse.getResults().stream()
                .anyMatch(result -> result.getDocument().getContent().toLowerCase().contains("machine learning"));
        
        assertTrue(foundRelevantResult, "Search results should contain machine learning content");
        
        System.out.println("Found " + searchResponse.getTotalResults() + " search results");
        searchResponse.getResults().forEach(result -> {
            System.out.println("Score: " + result.getScore() + ", Title: " + 
                result.getDocument().getMetadata().get("title"));
        });
    }
    
    @Test
    void testSimpleSearch() throws RagClientException {
        // First ensure we have some data by uploading a simple document
        String csvContent = "title,content\nTest Document,This is a test document for searching";
        
        CsvUploadRequest uploadRequest = CsvUploadRequest.builder()
                .csvContent(csvContent)
                .indexName(testIndexName + "-simple")
                .contentColumnName("content")
                .source("integration-test-simple")
                .build();
        
        ragClient.uploadCsv(uploadRequest);
        
        // Wait for indexing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Test simple search method
        SearchResponse response = ragClient.search("test document", testIndexName + "-simple");
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getResults());
    }
    
    @Test
    void testSearchWithParameters() throws RagClientException {
        // Upload test data
        String csvContent = "title,content\n" +
                "Document 1,First test document with some content\n" +
                "Document 2,Second test document with different content\n" +
                "Document 3,Third test document with more content";
        
        CsvUploadRequest uploadRequest = CsvUploadRequest.builder()
                .csvContent(csvContent)
                .indexName(testIndexName + "-params")
                .contentColumnName("content")
                .source("integration-test-params")
                .build();
        
        ragClient.uploadCsv(uploadRequest);
        
        // Wait for indexing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Test search with parameters
        SearchResponse response = ragClient.search("test document", testIndexName + "-params", 2, 0.1);
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().size() <= 2, "Should respect maxResults parameter");
    }
    
    @Test
    void testUploadSingleDocument() throws RagClientException {
        Document document = new Document();
        document.setId("integration-test-doc-1");
        document.setContent("This is a test document uploaded via the client library");
        document.setMetadata(Map.of(
                "title", "Integration Test Document",
                "author", "Test Suite",
                "category", "Testing"
        ));
        document.setSource("integration-test");
        
        String documentId = ragClient.uploadDocument(document, testIndexName + "-single");
        
        assertNotNull(documentId);
        assertEquals("integration-test-doc-1", documentId);
        
        System.out.println("Successfully uploaded document with ID: " + documentId);
    }
    
    @Test
    void testUploadMultipleDocuments() throws RagClientException {
        List<Document> documents = Arrays.asList(
                createTestDocument("doc1", "First batch document", "Batch Document 1"),
                createTestDocument("doc2", "Second batch document", "Batch Document 2"),
                createTestDocument("doc3", "Third batch document", "Batch Document 3")
        );
        
        List<String> documentIds = ragClient.uploadDocuments(documents, testIndexName + "-batch");
        
        assertNotNull(documentIds);
        assertEquals(3, documentIds.size());
        assertTrue(documentIds.contains("doc1"));
        assertTrue(documentIds.contains("doc2"));
        assertTrue(documentIds.contains("doc3"));
        
        System.out.println("Successfully uploaded " + documentIds.size() + " documents");
    }
    
    @Test
    void testSearchNonExistentIndex() {
        RagClientException exception = assertThrows(RagClientException.class, () -> {
            ragClient.search("test query", "non-existent-index-12345");
        });
        
        assertTrue(exception.hasStatusCode());
        // Could be 404 (not found) or other error codes depending on implementation
        assertTrue(exception.getStatusCode() >= 400);
        System.out.println("Expected error for non-existent index: " + exception.getMessage());
    }
    
    @Test
    void testFactoryMethods() {
        // Test factory methods work with actual service
        String baseUrl = System.getProperty("rag.service.url", "http://localhost:8080");
        
        try (RagClient client1 = RagClientImpl.create(baseUrl)) {
            assertTrue(client1.isHealthy());
        }
        
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl(baseUrl)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        try (RagClient client2 = RagClientImpl.create(config)) {
            assertTrue(client2.isHealthy());
        }
    }
    
    private Document createTestDocument(String id, String content, String title) {
        Document document = new Document();
        document.setId(id);
        document.setContent(content);
        document.setMetadata(Map.of(
                "title", title,
                "author", "Integration Test",
                "category", "Testing"
        ));
        document.setSource("integration-test-batch");
        return document;
    }
}
