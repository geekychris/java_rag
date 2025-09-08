package com.example.ragservice.controller;

import com.example.ragservice.dto.CsvUploadRequest;
import com.example.ragservice.dto.DocumentIngestionRequest;
import com.example.ragservice.dto.SearchRequest;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;
import com.example.ragservice.service.CsvProcessingService;
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
    
    @Autowired
    public RagController(VectorStoreService vectorStoreService, CsvProcessingService csvProcessingService) {
        this.vectorStoreService = vectorStoreService;
        this.csvProcessingService = csvProcessingService;
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
}
