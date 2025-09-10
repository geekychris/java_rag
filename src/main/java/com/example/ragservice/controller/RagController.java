package com.example.ragservice.controller;

import com.example.ragservice.dto.CsvUploadRequest;
import com.example.ragservice.dto.CsvFileIngestionRequest;
import com.example.ragservice.dto.DocumentIngestionRequest;
import com.example.ragservice.dto.EnhancedSearchResponse;
import com.example.ragservice.dto.SearchRequest;
import com.example.ragservice.dto.SemanticSummarizationRequest;
import com.example.ragservice.dto.SemanticSummarizationResponse;
import com.example.ragservice.dto.SimpleQuerySummarizationRequest;
import com.example.ragservice.dto.SummarizationRequest;
import com.example.ragservice.dto.SummarizationResponse;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;
import com.example.ragservice.service.CsvProcessingService;
import com.example.ragservice.service.SemanticSummarizationService;
import com.example.ragservice.service.SummarizationService;
import com.example.ragservice.service.VectorStoreService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final SemanticSummarizationService semanticSummarizationService;
    
    @Autowired
    public RagController(VectorStoreService vectorStoreService, 
                        CsvProcessingService csvProcessingService,
                        SummarizationService summarizationService,
                        SemanticSummarizationService semanticSummarizationService) {
        this.vectorStoreService = vectorStoreService;
        this.csvProcessingService = csvProcessingService;
        this.summarizationService = summarizationService;
        this.semanticSummarizationService = semanticSummarizationService;
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
                request.getDocIdColumnName(),
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
     * Ingest documents from CSV file (streaming)
     */
    @PostMapping("/documents/csv/file")
    public ResponseEntity<?> ingestCsvFile(@Valid @RequestBody CsvFileIngestionRequest request) {
        try {
            Path csvFile = Paths.get(request.getCsvFilePath());
            
            // Validate file exists and is readable
            if (!Files.exists(csvFile)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "CSV file not found: " + request.getCsvFilePath());
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!Files.isReadable(csvFile)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "CSV file is not readable: " + request.getCsvFilePath());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get file size for progress reporting
            long fileSize = Files.size(csvFile);
            
            logger.info("Starting CSV file ingestion: {} (size: {} bytes)", 
                       request.getCsvFilePath(), fileSize);
            
            // Process CSV file in streaming fashion
            int documentsIngested = csvProcessingService.ingestCsvFile(
                request.getCsvFilePath(),
                request.getIndexName(),
                request.getContentColumnName(),
                request.getDocIdColumnName(),
                request.getSource(),
                request.getBatchSize(),
                request.getMaxRecords()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documentsIngested", documentsIngested);
            response.put("indexName", request.getIndexName());
            response.put("filePath", request.getCsvFilePath());
            response.put("fileSize", fileSize);
            
            logger.info("Successfully ingested {} documents from CSV file {} into index {}", 
                       documentsIngested, request.getCsvFilePath(), request.getIndexName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to ingest CSV file: {}", request.getCsvFilePath(), e);
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
                request.getMinScore(),
                request.isIncludeEmbeddings()
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
    
    /**
     * Simple one-step summarization: query -> search -> summarize
     * Most common use case with sensible defaults
     */
    @PostMapping("/summarize-query")
    public ResponseEntity<?> summarizeQuery(@Valid @RequestBody SimpleQuerySummarizationRequest request) {
        try {
            logger.info("Received simple query summarization request: '{}'", request.getQuery());
            
            // Create semantic summarization request with good defaults
            SemanticSummarizationRequest semanticRequest = new SemanticSummarizationRequest(
                request.getQuery(), request.getIndexName()
            );
            semanticRequest.setMaxResults(request.getMaxResults() != null ? request.getMaxResults() : 10);
            semanticRequest.setMinScore(request.getMinScore() != null ? request.getMinScore() : 0.01);
            semanticRequest.setSearchType(SemanticSummarizationRequest.SearchType.VECTOR);
            semanticRequest.setIncludeSourceReferences(true);
            semanticRequest.setIncludeSearchResults(false); // Keep response clean
            semanticRequest.setMaxSummaryLength(request.getMaxSummaryLength());
            semanticRequest.setCustomPrompt(request.getCustomPrompt());
            
            SemanticSummarizationResponse response = semanticSummarizationService.searchAndSummarize(semanticRequest);
            
            if (response.isSuccess()) {
                // Return a simplified response format
                Map<String, Object> simplifiedResponse = new HashMap<>();
                simplifiedResponse.put("success", true);
                simplifiedResponse.put("query", request.getQuery());
                simplifiedResponse.put("summary", response.getSummary());
                simplifiedResponse.put("totalResults", response.getTotalResults());
                simplifiedResponse.put("sourceReferences", response.getSourceReferences());
                simplifiedResponse.put("processingTimeMs", response.getTotalProcessingTimeMs());
                simplifiedResponse.put("model", response.getModel());
                
                logger.info("Successfully completed simple query summarization for '{}' in {}ms", 
                           request.getQuery(), response.getTotalProcessingTimeMs());
                return ResponseEntity.ok(simplifiedResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", response.getError());
                errorResponse.put("query", request.getQuery());
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Simple query summarization failed for: '{}'", request.getQuery(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("query", request.getQuery());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Semantic search and summarization in one call - advanced version
     * This performs the semantic search internally and then summarizes the results
     */
    @PostMapping("/semantic-summarize")
    public ResponseEntity<SemanticSummarizationResponse> semanticSummarize(@Valid @RequestBody SemanticSummarizationRequest request) {
        try {
            logger.info("Received semantic summarization request for query: '{}' on index: '{}'", 
                       request.getQuery(), request.getIndexName());
            
            SemanticSummarizationResponse response = semanticSummarizationService.searchAndSummarize(request);
            
            if (response.isSuccess()) {
                logger.info("Successfully completed semantic summarization for query '{}' in {}ms (search: {}ms, summarization: {}ms)", 
                           request.getQuery(), response.getTotalProcessingTimeMs(), 
                           response.getSearchTimeMs(), response.getSummarizationTimeMs());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Semantic summarization failed for query '{}': {}", 
                           request.getQuery(), response.getError());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during semantic summarization for query: '{}'", request.getQuery(), e);
            
            SemanticSummarizationResponse errorResponse = new SemanticSummarizationResponse(
                request.getQuery(), request.getIndexName()
            );
            errorResponse.setSuccess(false);
            errorResponse.setError("Error: " + e.getMessage());
            errorResponse.setTotalProcessingTimeMs(0L);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
