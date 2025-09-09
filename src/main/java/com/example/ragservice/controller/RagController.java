package com.example.ragservice.controller;

import com.example.ragservice.dto.CsvUploadRequest;
import com.example.ragservice.dto.DocumentIngestionRequest;
import com.example.ragservice.dto.SearchRequest;
import com.example.ragservice.dto.SummarizationRequest;
import com.example.ragservice.dto.SummarizationResponse;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;
import com.example.ragservice.service.CsvProcessingService;
import com.example.ragservice.service.SummarizationService;
import com.example.ragservice.service.VectorStoreService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RagController {
    
    private static final Logger logger = LoggerFactory.getLogger(RagController.class);
    
    private final VectorStoreService vectorStoreService;
    private final CsvProcessingService csvProcessingService;
    private final SummarizationService summarizationService;
    
    @Autowired
    public RagController(VectorStoreService vectorStoreService, 
                        CsvProcessingService csvProcessingService,
                        SummarizationService summarizationService) {
        this.vectorStoreService = vectorStoreService;
        this.csvProcessingService = csvProcessingService;
        this.summarizationService = summarizationService;
    }
    
    /**
     * Ingest a single document
     */
    @PostMapping("/documents")
    public ResponseEntity<?> ingestDocument(@Valid @RequestBody DocumentIngestionRequest request) {
        try {
            Document document = new Document(
                UUID.randomUUID().toString(),
                request.getContent(),
                request.getMetadata()
            );
            document.setSource(request.getSource());
            
            String documentId = vectorStoreService.storeDocument(request.getIndexName(), document);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documentId", documentId);
            response.put("indexName", request.getIndexName());
            
            logger.info("Successfully ingested document {} into index {}", documentId, request.getIndexName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to ingest document", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Ingest documents from CSV
     */
    @PostMapping("/documents/csv")
    public ResponseEntity<?> ingestCsv(@Valid @RequestBody CsvUploadRequest request) {
        try {
            // Validate CSV content
            if (!csvProcessingService.isValidCsv(request.getCsvContent())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Invalid CSV format");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Parse CSV to documents
            List<Document> documents = csvProcessingService.parseCsvToDocuments(
                request.getCsvContent(),
                request.getContentColumnName(),
                request.getSource()
            );
            
            if (documents.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "No documents found in CSV");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Store documents
            vectorStoreService.storeDocuments(request.getIndexName(), documents);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documentsIngested", documents.size());
            response.put("indexName", request.getIndexName());
            response.put("headers", csvProcessingService.getCsvHeaders(request.getCsvContent()));
            
            logger.info("Successfully ingested {} documents from CSV into index {}", 
                       documents.size(), request.getIndexName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to ingest CSV documents", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Search documents using vector similarity
     */
    @PostMapping("/search")
    public ResponseEntity<?> search(@Valid @RequestBody SearchRequest request) {
        try {
            List<SearchResult> results = vectorStoreService.searchSimilar(
                request.getIndexName(),
                request.getQuery(),
                request.getSize(),
                request.getMinScore()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", request.getQuery());
            response.put("results", results);
            response.put("totalResults", results.size());
            
            logger.debug("Vector search returned {} results for query: {}", 
                        results.size(), request.getQuery());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Search failed for query: {}", request.getQuery(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Search documents using hybrid search (vector + text)
     */
    @PostMapping("/search/hybrid")
    public ResponseEntity<?> hybridSearch(@Valid @RequestBody SearchRequest request) {
        try {
            List<SearchResult> results = vectorStoreService.hybridSearch(
                request.getIndexName(),
                request.getQuery(),
                request.getSize(),
                request.getMinScore()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", request.getQuery());
            response.put("results", results);
            response.put("totalResults", results.size());
            
            logger.debug("Hybrid search returned {} results for query: {}", 
                        results.size(), request.getQuery());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Hybrid search failed for query: {}", request.getQuery(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get a specific document by ID
     */
    @GetMapping("/documents/{indexName}/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable String indexName, @PathVariable String documentId) {
        try {
            Document document = vectorStoreService.getDocument(indexName, documentId);
            
            if (document == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Document not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("document", document);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve document {} from index {}", documentId, indexName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Generate a summary based on search results and the original query
     */
    @PostMapping("/summarize")
    public ResponseEntity<SummarizationResponse> summarize(@Valid @RequestBody SummarizationRequest request) {
        try {
            logger.info("Received summarization request for query: '{}' with {} search results", 
                       request.getQuery(), request.getSearchResults().size());
            
            SummarizationResponse response = summarizationService.summarize(request);
            
            if (response.isSuccess()) {
                logger.info("Successfully generated summary for query '{}' in {}ms", 
                           request.getQuery(), response.getProcessingTimeMs());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Summarization failed for query '{}': {}", 
                           request.getQuery(), response.getSummary());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during summarization for query: '{}'", request.getQuery(), e);
            
            SummarizationResponse errorResponse = new SummarizationResponse(
                request.getQuery(),
                "Error: " + e.getMessage(),
                false,
                request.getSearchResults() != null ? request.getSearchResults().size() : 0
            );
            errorResponse.setProcessingTimeMs(0L);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Search and summarize in one call - convenience endpoint
     */
    @PostMapping("/search-and-summarize")
    public ResponseEntity<?> searchAndSummarize(@Valid @RequestBody SearchRequest searchRequest) {
        try {
            logger.info("Received search-and-summarize request for query: '{}'", searchRequest.getQuery());
            
            // First, perform the search
            List<SearchResult> searchResults = vectorStoreService.searchSimilar(
                searchRequest.getIndexName(),
                searchRequest.getQuery(),
                searchRequest.getSize(),
                searchRequest.getMinScore()
            );
            
            if (searchResults.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "No search results found to summarize");
                response.put("query", searchRequest.getQuery());
                response.put("searchResults", searchResults);
                response.put("summary", null);
                return ResponseEntity.ok(response);
            }
            
            // Then, generate the summary
            SummarizationRequest summaryRequest = new SummarizationRequest(
                searchRequest.getQuery(),
                searchResults
            );
            summaryRequest.setIncludeSourceReferences(true);
            
            SummarizationResponse summaryResponse = summarizationService.summarize(summaryRequest);
            
            // Combine search and summary results
            Map<String, Object> response = new HashMap<>();
            response.put("success", summaryResponse.isSuccess());
            response.put("query", searchRequest.getQuery());
            response.put("searchResults", searchResults);
            response.put("totalResults", searchResults.size());
            response.put("summary", summaryResponse.getSummary());
            response.put("sourceReferences", summaryResponse.getSourceReferences());
            response.put("model", summaryResponse.getModel());
            response.put("processingTimeMs", summaryResponse.getProcessingTimeMs());
            
            logger.info("Successfully completed search-and-summarize for query '{}'", searchRequest.getQuery());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Search-and-summarize failed for query: {}", searchRequest.getQuery(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("query", searchRequest.getQuery());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
