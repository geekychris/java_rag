package com.example.ragservice.controller;

import com.example.ragservice.dto.CsvUploadRequest;
import com.example.ragservice.dto.DocumentIngestionRequest;
import com.example.ragservice.dto.SearchRequest;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;
import com.example.ragservice.service.CsvProcessingService;
import com.example.ragservice.service.VectorStoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VectorStoreService vectorStoreService;

    @MockBean
    private CsvProcessingService csvProcessingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testIngestDocument_Success() throws Exception {
        // Given
        DocumentIngestionRequest request = new DocumentIngestionRequest();
        request.setContent("Test document content");
        request.setIndexName("test-index");
        request.setSource("test-source");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        request.setMetadata(metadata);

        when(vectorStoreService.storeDocument(eq("test-index"), any(Document.class)))
                .thenReturn("doc-123");

        // When & Then
        mockMvc.perform(post("/api/rag/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").value("doc-123"))
                .andExpect(jsonPath("$.indexName").value("test-index"));
    }

    @Test
    void testIngestDocument_ValidationError() throws Exception {
        // Given - invalid request with empty content
        DocumentIngestionRequest request = new DocumentIngestionRequest();
        request.setContent(""); // Empty content should fail validation
        request.setIndexName("test-index");

        // When & Then
        mockMvc.perform(post("/api/rag/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIngestCsv_Success() throws Exception {
        // Given
        String csvContent = """
            title,content
            "Doc1","Content1"
            "Doc2","Content2"
            """;
        
        CsvUploadRequest request = new CsvUploadRequest();
        request.setCsvContent(csvContent);
        request.setIndexName("test-index");
        request.setContentColumnName("content");
        request.setSource("csv-source");

        Document doc1 = new Document("id1", "Content1", new HashMap<>());
        Document doc2 = new Document("id2", "Content2", new HashMap<>());
        List<Document> documents = Arrays.asList(doc1, doc2);

        when(csvProcessingService.isValidCsv(csvContent)).thenReturn(true);
        when(csvProcessingService.parseCsvToDocuments(csvContent, "content", "csv-source"))
                .thenReturn(documents);
        when(csvProcessingService.getCsvHeaders(csvContent))
                .thenReturn(Arrays.asList("title", "content"));

        // When & Then
        mockMvc.perform(post("/api/rag/documents/csv")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentsIngested").value(2))
                .andExpect(jsonPath("$.indexName").value("test-index"));
    }

    @Test
    void testIngestCsv_InvalidCsv() throws Exception {
        // Given
        CsvUploadRequest request = new CsvUploadRequest();
        request.setCsvContent("invalid csv content");
        request.setIndexName("test-index");

        when(csvProcessingService.isValidCsv("invalid csv content")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/rag/documents/csv")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid CSV format"));
    }

    @Test
    void testSearch_Success() throws Exception {
        // Given
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");
        request.setIndexName("test-index");
        request.setSize(5);
        request.setMinScore(0.5);

        Document doc = new Document("id1", "Test content", new HashMap<>());
        SearchResult result = new SearchResult(doc, 0.8);
        List<SearchResult> results = Arrays.asList(result);

        when(vectorStoreService.searchSimilar("test-index", "test query", 5, 0.5))
                .thenReturn(results);

        // When & Then
        mockMvc.perform(post("/api/rag/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.query").value("test query"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].score").value(0.8));
    }

    @Test
    void testHybridSearch_Success() throws Exception {
        // Given
        SearchRequest request = new SearchRequest();
        request.setQuery("hybrid query");
        request.setIndexName("test-index");
        request.setSize(10);

        Document doc = new Document("id1", "Hybrid content", new HashMap<>());
        SearchResult result = new SearchResult(doc, 0.9);
        List<SearchResult> results = Arrays.asList(result);

        when(vectorStoreService.hybridSearch("test-index", "hybrid query", 10, 0.0))
                .thenReturn(results);

        // When & Then
        mockMvc.perform(post("/api/rag/search/hybrid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.query").value("hybrid query"))
                .andExpect(jsonPath("$.totalResults").value(1));
    }

    @Test
    void testGetDocument_Success() throws Exception {
        // Given
        String indexName = "test-index";
        String documentId = "doc-123";
        
        Document document = new Document(documentId, "Test content", new HashMap<>());
        
        when(vectorStoreService.getDocument(indexName, documentId)).thenReturn(document);

        // When & Then
        mockMvc.perform(get("/api/rag/documents/{indexName}/{documentId}", indexName, documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.document.id").value(documentId))
                .andExpect(jsonPath("$.document.content").value("Test content"));
    }

    @Test
    void testGetDocument_NotFound() throws Exception {
        // Given
        String indexName = "test-index";
        String documentId = "non-existent";
        
        when(vectorStoreService.getDocument(indexName, documentId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/rag/documents/{indexName}/{documentId}", indexName, documentId))
                .andExpect(status().isNotFound());
    }
}
