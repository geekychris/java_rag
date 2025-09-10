package com.example.ragservice.controller;

import com.example.ragservice.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/indexes")
@CrossOrigin(origins = "*")
public class IndexController {
    
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    
    private final VectorStoreService vectorStoreService;
    
    @Autowired
    public IndexController(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }
    
    /**
     * Get all available indexes
     */
    @GetMapping
    public ResponseEntity<?> listIndexes() {
        try {
            java.util.List<String> indexes = vectorStoreService.listIndexes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("indexes", indexes);
            response.put("count", indexes.size());
            
            logger.debug("Listed {} indexes", indexes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to list indexes", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Create a new index
     */
    @PostMapping("/{indexName}")
    public ResponseEntity<?> createIndex(@PathVariable String indexName) {
        try {
            vectorStoreService.createIndex(indexName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("indexName", indexName);
            response.put("message", "Index created successfully");
            
            logger.info("Created index: {}", indexName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to create index: {}", indexName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check if an index exists
     */
    @GetMapping("/{indexName}/exists")
    public ResponseEntity<?> indexExists(@PathVariable String indexName) {
        try {
            boolean exists = vectorStoreService.indexExists(indexName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("indexName", indexName);
            response.put("exists", exists);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to check if index exists: {}", indexName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete an index
     */
    @DeleteMapping("/{indexName}")
    public ResponseEntity<?> deleteIndex(@PathVariable String indexName) {
        try {
            vectorStoreService.deleteIndex(indexName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("indexName", indexName);
            response.put("message", "Index deleted successfully");
            
            logger.info("Deleted index: {}", indexName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to delete index: {}", indexName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
