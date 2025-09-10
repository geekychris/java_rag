package com.example.ragservice.controller;

import com.example.ragservice.dto.*;
import com.example.ragservice.service.DirectoryScanService;
import com.example.ragservice.service.CsvStreamingService;
import com.example.ragservice.service.DocumentExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/document-processing")
@Tag(name = "Document Processing", description = "Document extraction and CSV streaming APIs")
public class DocumentScanController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentScanController.class);
    
    @Autowired
    private DirectoryScanService directoryScanService;
    
    @Autowired
    private CsvStreamingService csvStreamingService;
    
    @Autowired
    private DocumentExtractionService documentExtractionService;
    
    /**
     * Start a directory scan operation
     */
    @PostMapping("/directory-scan")
    @Operation(summary = "Start directory scanning", 
               description = "Scans a directory for supported document files and extracts text to CSV")
    public ResponseEntity<DirectoryScanResponse> startDirectoryScan(
            @Valid @RequestBody DirectoryScanRequest request) {
        
        logger.info("Starting directory scan for path: {}", request.getDirectoryPath());
        
        try {
            DirectoryScanResponse response = directoryScanService.startDirectoryScan(request);
            return ResponseEntity.accepted().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid directory scan request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to start directory scan", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get the status of a directory scan operation
     */
    @GetMapping("/directory-scan/{scanId}")
    @Operation(summary = "Get directory scan status", 
               description = "Retrieves the current status of a directory scan operation")
    public ResponseEntity<DirectoryScanResponse> getDirectoryScanStatus(@PathVariable String scanId) {
        
        DirectoryScanResponse response = directoryScanService.getScanStatus(scanId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a directory scan operation
     */
    @DeleteMapping("/directory-scan/{scanId}")
    @Operation(summary = "Cancel directory scan", 
               description = "Cancels an active directory scan operation")
    public ResponseEntity<Map<String, Object>> cancelDirectoryScan(@PathVariable String scanId) {
        
        boolean cancelled = directoryScanService.cancelScan(scanId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("scan_id", scanId);
        result.put("cancelled", cancelled);
        
        if (cancelled) {
            logger.info("Directory scan cancelled: {}", scanId);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * List all active directory scan operations
     */
    @GetMapping("/directory-scan")
    @Operation(summary = "List active directory scans", 
               description = "Lists all currently active directory scan operations")
    public ResponseEntity<Map<String, DirectoryScanResponse>> listActiveDirectoryScans() {
        
        Map<String, DirectoryScanResponse> activeScans = directoryScanService.getActiveScanOperations();
        return ResponseEntity.ok(activeScans);
    }
    
    /**
     * Start a CSV streaming operation
     */
    @PostMapping("/csv-streaming")
    @Operation(summary = "Start CSV streaming", 
               description = "Starts streaming a CSV file for indexing with support for large files")
    public ResponseEntity<CsvStreamingResponse> startCsvStreaming(
            @Valid @RequestBody CsvStreamingRequest request) {
        
        logger.info("Starting CSV streaming for file: {}", request.getCsvFilePath());
        
        try {
            CsvStreamingResponse response = csvStreamingService.startCsvStreaming(request);
            return ResponseEntity.accepted().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid CSV streaming request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to start CSV streaming", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get the status of a CSV streaming operation
     */
    @GetMapping("/csv-streaming/{streamId}")
    @Operation(summary = "Get CSV streaming status", 
               description = "Retrieves the current status of a CSV streaming operation")
    public ResponseEntity<CsvStreamingResponse> getCsvStreamingStatus(@PathVariable String streamId) {
        
        CsvStreamingResponse response = csvStreamingService.getStreamStatus(streamId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a CSV streaming operation
     */
    @DeleteMapping("/csv-streaming/{streamId}")
    @Operation(summary = "Cancel CSV streaming", 
               description = "Cancels an active CSV streaming operation")
    public ResponseEntity<Map<String, Object>> cancelCsvStreaming(@PathVariable String streamId) {
        
        boolean cancelled = csvStreamingService.cancelStream(streamId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("stream_id", streamId);
        result.put("cancelled", cancelled);
        
        if (cancelled) {
            logger.info("CSV streaming cancelled: {}", streamId);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * List all active CSV streaming operations
     */
    @GetMapping("/csv-streaming")
    @Operation(summary = "List active CSV streams", 
               description = "Lists all currently active CSV streaming operations")
    public ResponseEntity<Map<String, CsvStreamingResponse>> listActiveCsvStreams() {
        
        Map<String, CsvStreamingResponse> activeStreams = csvStreamingService.getActiveStreamOperations();
        return ResponseEntity.ok(activeStreams);
    }
    
    /**
     * Get information about supported document formats
     */
    @GetMapping("/supported-formats")
    @Operation(summary = "Get supported document formats", 
               description = "Lists all document formats supported for text extraction")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        
        Set<String> supportedExtensions = documentExtractionService.getSupportedExtensions();
        
        Map<String, Object> result = new HashMap<>();
        result.put("supported_extensions", supportedExtensions);
        result.put("total_count", supportedExtensions.size());
        result.put("description", "Supported document formats for text extraction");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Estimate the number of records in a CSV file
     */
    @PostMapping("/csv-estimate")
    @Operation(summary = "Estimate CSV record count", 
               description = "Estimates the number of records in a CSV file without fully loading it")
    public ResponseEntity<Map<String, Object>> estimateCsvRecords(
            @RequestBody Map<String, Object> request) {
        
        String csvFilePath = (String) request.get("csv_file_path");
        Boolean skipHeader = (Boolean) request.getOrDefault("skip_header", true);
        
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            long estimatedCount = csvStreamingService.estimateRecordCount(csvFilePath, skipHeader);
            
            Map<String, Object> result = new HashMap<>();
            result.put("csv_file_path", csvFilePath);
            result.put("estimated_record_count", estimatedCount);
            result.put("skip_header", skipHeader);
            
            if (estimatedCount < 0) {
                result.put("status", "unable_to_estimate");
                result.put("message", "Could not estimate record count");
            } else {
                result.put("status", "success");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to estimate CSV record count for file: {}", csvFilePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check endpoint for document processing services
     */
    @GetMapping("/health")
    @Operation(summary = "Document processing health check", 
               description = "Checks the health of document processing services")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("services", Map.of(
            "directory_scan", "available",
            "csv_streaming", "available",
            "document_extraction", "available"
        ));
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Global exception handler for this controller
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        logger.error("Unhandled exception in DocumentScanController", e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Internal server error");
        error.put("message", e.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
