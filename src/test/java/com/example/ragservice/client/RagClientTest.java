package com.example.ragservice.client;

import com.example.ragservice.client.config.RagClientConfig;
import com.example.ragservice.client.dto.SearchRequest;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.client.exception.RagClientException;
import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RagClientTest {
    
    private MockWebServer mockWebServer;
    private RagClient ragClient;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl(baseUrl)
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        
        ragClient = new RagClientImpl(config);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @AfterEach
    void tearDown() throws IOException {
        ragClient.close();
        mockWebServer.shutdown();
    }
    
    @Test
    void testSearchWithRequest() throws Exception {
        // Arrange
        SearchRequest request = SearchRequest.builder()
                .query("machine learning")
                .indexName("test-index")
                .maxResults(5)
                .minScore(0.5)
                .build();
        
        SearchResponse expectedResponse = createMockSearchResponse();
        String responseJson = objectMapper.writeValueAsString(expectedResponse);
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        // Act
        SearchResponse response = ragClient.search(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("machine learning", response.getQuery());
        assertTrue(response.isSuccess());
        assertEquals(2, response.getTotalResults());
        assertNotNull(response.getResults());
        assertEquals(2, response.getResults().size());
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/rag/search", recordedRequest.getPath());
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
        
        // Verify request body
        SearchRequest actualRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(), SearchRequest.class);
        assertEquals(request.getQuery(), actualRequest.getQuery());
        assertEquals(request.getIndexName(), actualRequest.getIndexName());
        assertEquals(request.getMaxResults(), actualRequest.getMaxResults());
        assertEquals(request.getMinScore(), actualRequest.getMinScore());
    }
    
    @Test
    void testSearchSimple() throws Exception {
        // Arrange
        SearchResponse expectedResponse = createMockSearchResponse();
        String responseJson = objectMapper.writeValueAsString(expectedResponse);
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        // Act
        SearchResponse response = ragClient.search("test query", "test-index");
        
        // Assert
        assertNotNull(response);
        assertEquals("machine learning", response.getQuery());
        assertTrue(response.isSuccess());
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SearchRequest actualRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(), SearchRequest.class);
        assertEquals("test query", actualRequest.getQuery());
        assertEquals("test-index", actualRequest.getIndexName());
    }
    
    @Test
    void testSearchWithParameters() throws Exception {
        // Arrange
        SearchResponse expectedResponse = createMockSearchResponse();
        String responseJson = objectMapper.writeValueAsString(expectedResponse);
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        // Act
        SearchResponse response = ragClient.search("test query", "test-index", 10, 0.3);
        
        // Assert
        assertNotNull(response);
        
        // Verify request parameters
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SearchRequest actualRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(), SearchRequest.class);
        assertEquals("test query", actualRequest.getQuery());
        assertEquals("test-index", actualRequest.getIndexName());
        assertEquals(10, actualRequest.getMaxResults());
        assertEquals(0.3, actualRequest.getMinScore());
    }
    
    @Test
    void testSearchFailure() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"error\":\"Index not found\"}")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(404));
        
        // Act & Assert
        RagClientException exception = assertThrows(RagClientException.class, () -> {
            ragClient.search("test query", "non-existent-index");
        });
        
        assertEquals(404, exception.getStatusCode());
        assertTrue(exception.getErrorBody().contains("Index not found"));
    }
    
    @Test
    void testUploadCsv() throws Exception {
        // Arrange
        CsvUploadRequest request = CsvUploadRequest.builder()
                .csvContent("title,content\\nTest,This is test content")
                .indexName("test-index")
                .contentColumnName("content")
                .source("unit-test")
                .build();
        
        CsvUploadResponse expectedResponse = createMockCsvUploadResponse();
        String responseJson = objectMapper.writeValueAsString(expectedResponse);
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        // Act
        CsvUploadResponse response = ragClient.uploadCsv(request);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getDocumentsIndexed());
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/rag/documents/csv", recordedRequest.getPath());
        
        CsvUploadRequest actualRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(), CsvUploadRequest.class);
        assertEquals(request.getCsvContent(), actualRequest.getCsvContent());
        assertEquals(request.getIndexName(), actualRequest.getIndexName());
    }
    
    @Test
    void testUploadCsvFile() throws Exception {
        // Arrange
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.writeString(tempFile, "title,content\\nTest Article,This is test content");
        
        CsvUploadResponse expectedResponse = createMockCsvUploadResponse();
        String responseJson = objectMapper.writeValueAsString(expectedResponse);
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        try {
            // Act
            CsvUploadResponse response = ragClient.uploadCsvFile(tempFile.toFile(), "test-index");
            
            // Assert
            assertNotNull(response);
            assertTrue(response.isSuccess());
            
            // Verify request
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            CsvUploadRequest actualRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(), CsvUploadRequest.class);
            assertTrue(actualRequest.getCsvContent().contains("This is test content"));
            assertEquals("test-index", actualRequest.getIndexName());
            assertEquals("content", actualRequest.getContentColumnName());
            assertEquals("client-file-upload", actualRequest.getSource());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    @Test
    void testUploadDocument() throws Exception {
        // Arrange
        Document document = new Document("doc1", "Test document content", Map.of("title", "Test Doc"));
        
        String responseJson = "{\"documentId\":\"doc1\",\"success\":true}";
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(201));
        
        // Act
        String documentId = ragClient.uploadDocument(document, "test-index");
        
        // Assert
        assertEquals("doc1", documentId);
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/rag/documents/test-index", recordedRequest.getPath());
        
        Document actualDocument = objectMapper.readValue(recordedRequest.getBody().readUtf8(), Document.class);
        assertEquals(document.getId(), actualDocument.getId());
        assertEquals(document.getContent(), actualDocument.getContent());
    }
    
    @Test
    void testUploadDocuments() throws Exception {
        // Arrange
        List<Document> documents = Arrays.asList(
                new Document("doc1", "Content 1", Map.of()),
                new Document("doc2", "Content 2", Map.of())
        );
        
        String responseJson = "{\"documentIds\":[\"doc1\",\"doc2\"],\"success\":true}";
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(201));
        
        // Act
        List<String> documentIds = ragClient.uploadDocuments(documents, "test-index");
        
        // Assert
        assertEquals(2, documentIds.size());
        assertTrue(documentIds.contains("doc1"));
        assertTrue(documentIds.contains("doc2"));
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/rag/documents/test-index/batch", recordedRequest.getPath());
    }
    
    @Test
    void testIsHealthy() throws Exception {
        // Arrange - healthy service
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        
        // Act & Assert
        assertTrue(ragClient.isHealthy());
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/actuator/health", recordedRequest.getPath());
    }
    
    @Test
    void testIsHealthyWhenUnhealthy() throws Exception {
        // Arrange - unhealthy service
        mockWebServer.enqueue(new MockResponse().setResponseCode(503));
        
        // Act & Assert
        assertFalse(ragClient.isHealthy());
    }
    
    @Test
    void testGetServiceInfo() throws Exception {
        // Arrange
        String serviceInfo = "{\"app\":{\"name\":\"rag-service\",\"version\":\"1.0.0\"}}";
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(serviceInfo)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        // Act
        String info = ragClient.getServiceInfo();
        
        // Assert
        assertEquals(serviceInfo, info);
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/actuator/info", recordedRequest.getPath());
    }
    
    @Test
    void testConfigurationBuilder() {
        // Test configuration builder
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl("https://api.example.com")
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .maxRetries(5)
                .retryDelay(Duration.ofSeconds(2))
                .build();
        
        assertEquals("https://api.example.com", config.getBaseUrl());
        assertEquals(Duration.ofSeconds(10), config.getConnectTimeout());
        assertEquals(Duration.ofSeconds(30), config.getReadTimeout());
        assertEquals(5, config.getMaxRetries());
        assertEquals(Duration.ofSeconds(2), config.getRetryDelay());
    }
    
    @Test
    void testFactoryMethods() {
        // Test default factory method
        RagClient client1 = RagClientImpl.create();
        assertNotNull(client1);
        client1.close();
        
        // Test factory method with base URL
        RagClient client2 = RagClientImpl.create("http://localhost:9999");
        assertNotNull(client2);
        client2.close();
        
        // Test factory method with config
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl("http://localhost:8888")
                .build();
        RagClient client3 = RagClientImpl.create(config);
        assertNotNull(client3);
        client3.close();
    }
    
    private SearchResponse createMockSearchResponse() {
        SearchResponse response = new SearchResponse();
        response.setQuery("machine learning");
        response.setSuccess(true);
        response.setTotalResults(2);
        
        // Create mock search results
        Document doc1 = new Document("doc1", "Machine learning content", Map.of("title", "ML Article"));
        SearchResult result1 = new SearchResult(doc1, 0.95);
        
        Document doc2 = new Document("doc2", "AI and ML content", Map.of("title", "AI Article"));
        SearchResult result2 = new SearchResult(doc2, 0.85);
        
        response.setResults(Arrays.asList(result1, result2));
        
        return response;
    }
    
    private CsvUploadResponse createMockCsvUploadResponse() {
        CsvUploadResponse response = new CsvUploadResponse();
        response.setSuccess(true);
        response.setDocumentsIndexed(1);
        response.setIndexName("test-index");
        response.setCsvHeaders(Arrays.asList("title", "content"));
        return response;
    }
}
