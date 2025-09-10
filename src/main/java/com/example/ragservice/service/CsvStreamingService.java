package com.example.ragservice.service;

import com.example.ragservice.dto.CsvStreamingRequest;
import com.example.ragservice.dto.CsvStreamingResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsvStreamingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvStreamingService.class);
    
    @Value("${document.processing.csv.batch-size:100}")
    private int defaultBatchSize;
    
    @Value("${document.processing.csv.max-record-size:1048576}")
    private int maxRecordSize; // 1MB default
    
    // Store active streaming operations
    private final Map<String, CsvStreamingResponse> activeStreamOperations = new ConcurrentHashMap<>();
    
    /**
     * Starts a CSV streaming operation asynchronously
     *
     * @param request the streaming request
     * @return the streaming response with initial status
     */
    public CsvStreamingResponse startCsvStreaming(CsvStreamingRequest request) {
        String streamId = generateStreamId();
        CsvStreamingResponse response = new CsvStreamingResponse(streamId, "STARTED", request.getIndexName());
        
        activeStreamOperations.put(streamId, response);
        
        // Start the streaming asynchronously
        CompletableFuture.runAsync(() -> performCsvStreaming(streamId, request));
        
        return response;
    }
    
    /**
     * Gets the status of an active streaming operation
     *
     * @param streamId the stream ID
     * @return the current streaming status
     */
    public CsvStreamingResponse getStreamStatus(String streamId) {
        return activeStreamOperations.get(streamId);
    }
    
    /**
     * Lists all active streaming operations
     *
     * @return map of active streams
     */
    public Map<String, CsvStreamingResponse> getActiveStreamOperations() {
        return new HashMap<>(activeStreamOperations);
    }
    
    /**
     * Cancels an active streaming operation
     *
     * @param streamId the stream ID
     * @return true if cancelled, false if not found
     */
    public boolean cancelStream(String streamId) {
        CsvStreamingResponse response = activeStreamOperations.get(streamId);
        if (response != null && !"COMPLETED".equals(response.getStatus()) && !"FAILED".equals(response.getStatus())) {
            response.setStatus("CANCELLED");
            response.setEndTime(LocalDateTime.now());
            response.setDurationMs(System.currentTimeMillis() - 
                response.getStartTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());
            return true;
        }
        return false;
    }
    
    /**
     * Estimates the number of records in a CSV file without loading it entirely
     *
     * @param csvFilePath path to the CSV file
     * @param skipHeader whether to skip the header row
     * @return estimated number of records
     */
    public long estimateRecordCount(String csvFilePath, boolean skipHeader) {
        try {
            long fileSize = Files.size(Paths.get(csvFilePath));
            long lineCount = Files.lines(Paths.get(csvFilePath)).count();
            
            if (skipHeader && lineCount > 0) {
                lineCount--;
            }
            
            return lineCount;
        } catch (IOException e) {
            logger.warn("Failed to estimate record count for file: {}", csvFilePath, e);
            return -1; // Unknown count
        }
    }
    
    private void performCsvStreaming(String streamId, CsvStreamingRequest request) {
        CsvStreamingResponse response = activeStreamOperations.get(streamId);
        long startTime = System.currentTimeMillis();
        
        try {
            response.setStatus("PROCESSING");
            
            // Validate CSV file exists
            if (!Files.exists(Paths.get(request.getCsvFilePath()))) {
                throw new IllegalArgumentException("CSV file does not exist: " + request.getCsvFilePath());
            }
            
            // Estimate total records
            long estimatedRecords = estimateRecordCount(request.getCsvFilePath(), request.isSkipHeader());
            if (estimatedRecords > 0) {
                response.setTotalRecords(estimatedRecords);
            }
            
            // Process CSV file in batches
            processCsvInBatches(streamId, request);
            
            // Update final status
            CsvStreamingResponse finalResponse = activeStreamOperations.get(streamId);
            if ("CANCELLED".equals(finalResponse.getStatus())) {
                return;
            }
            
            finalResponse.setStatus("COMPLETED");
            finalResponse.setEndTime(LocalDateTime.now());
            long duration = System.currentTimeMillis() - startTime;
            finalResponse.setDurationMs(duration);
            
            // Calculate processing rate
            if (duration > 0) {
                double rate = (finalResponse.getRecordsProcessed() * 1000.0) / duration;
                finalResponse.setProcessingRatePerSecond(rate);
            }
            
            logger.info("CSV streaming completed. Stream ID: {}, Records processed: {}, Duration: {}ms", 
                streamId, finalResponse.getRecordsProcessed(), duration);
            
        } catch (Exception e) {
            logger.error("CSV streaming failed. Stream ID: {}", streamId, e);
            response.setStatus("FAILED");
            response.setEndTime(LocalDateTime.now());
            response.setDurationMs(System.currentTimeMillis() - startTime);
            
            List<String> errors = response.getErrors();
            if (errors == null) {
                errors = new ArrayList<>();
                response.setErrors(errors);
            }
            errors.add("Streaming failed: " + e.getMessage());
        }
    }
    
    private void processCsvInBatches(String streamId, CsvStreamingRequest request) throws IOException {
        CsvStreamingResponse response = activeStreamOperations.get(streamId);
        
        // Configure CSV format
        CSVFormat csvFormat = CSVFormat.DEFAULT
            .withDelimiter(request.getDelimiter())
            .withQuote(request.getQuoteCharacter())
            .withEscape(request.getEscapeCharacter());
        
        if (request.isSkipHeader()) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }
        
        AtomicLong recordsProcessed = new AtomicLong(0);
        AtomicLong recordsIndexed = new AtomicLong(0);
        AtomicLong recordsFailed = new AtomicLong(0);
        AtomicLong batchCount = new AtomicLong(0);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try (FileReader fileReader = new FileReader(request.getCsvFilePath());
             CSVParser csvParser = new CSVParser(fileReader, csvFormat)) {
            
            List<Map<String, Object>> batch = new ArrayList<>();
            
            for (CSVRecord record : csvParser) {
                if ("CANCELLED".equals(response.getStatus())) {
                    break;
                }
                
                try {
                    Map<String, Object> documentData = processCsvRecord(record, request);
                    if (documentData != null) {
                        batch.add(documentData);
                        recordsProcessed.incrementAndGet();
                    }
                    
                    // Process batch when it reaches the batch size
                    if (batch.size() >= request.getBatchSize()) {
                        long indexed = processBatch(batch, request, streamId);
                        recordsIndexed.addAndGet(indexed);
                        recordsFailed.addAndGet(batch.size() - indexed);
                        batchCount.incrementAndGet();
                        
                        batch.clear();
                        
                        // Update progress
                        updateProgress(response, recordsProcessed.get(), recordsIndexed.get(), 
                            recordsFailed.get(), batchCount.get());
                    }
                    
                } catch (Exception e) {
                    recordsFailed.incrementAndGet();
                    String errorMsg = "Failed to process record " + record.getRecordNumber() + ": " + e.getMessage();
                    logger.debug(errorMsg);
                    
                    if (errors.size() < 100) { // Limit error collection
                        errors.add(errorMsg);
                    }
                }
            }
            
            // Process remaining records in the last batch
            if (!batch.isEmpty() && !"CANCELLED".equals(response.getStatus())) {
                long indexed = processBatch(batch, request, streamId);
                recordsIndexed.addAndGet(indexed);
                recordsFailed.addAndGet(batch.size() - indexed);
                batchCount.incrementAndGet();
            }
            
            // Update final counts
            response.setRecordsProcessed(recordsProcessed.get());
            response.setRecordsIndexed(recordsIndexed.get());
            response.setRecordsFailed(recordsFailed.get());
            response.setBatchCount(batchCount.get());
            response.setErrors(errors);
            response.setWarnings(warnings);
            
        }
    }
    
    private Map<String, Object> processCsvRecord(CSVRecord record, CsvStreamingRequest request) {
        Map<String, Object> documentData = new HashMap<>();
        
        // Get the text content
        String textContent = record.get(request.getTextColumn());
        if (textContent == null || textContent.trim().isEmpty()) {
            return null; // Skip empty text records
        }
        
        // Check record size limit
        if (textContent.length() > maxRecordSize) {
            logger.warn("Record {} exceeds maximum size, truncating", record.getRecordNumber());
            textContent = textContent.substring(0, maxRecordSize);
        }
        
        documentData.put("text", textContent);
        documentData.put("record_number", record.getRecordNumber());
        
        // Add metadata columns
        if (request.getMetadataColumns() != null) {
            Map<String, Object> metadata = new HashMap<>();
            for (String metadataColumn : request.getMetadataColumns()) {
                try {
                    String value = record.get(metadataColumn);
                    if (value != null) {
                        metadata.put(metadataColumn, value);
                    }
                } catch (IllegalArgumentException e) {
                    // Column not found, skip
                    logger.debug("Metadata column '{}' not found in record {}", metadataColumn, record.getRecordNumber());
                }
            }
            documentData.put("metadata", metadata);
        }
        
        // Add all available columns as metadata
        Map<String, Object> allColumns = new HashMap<>();
        for (String header : record.getParser().getHeaderNames()) {
            try {
                String value = record.get(header);
                if (value != null) {
                    allColumns.put(header, value);
                }
            } catch (IllegalArgumentException e) {
                // Skip if column not accessible
            }
        }
        documentData.put("all_columns", allColumns);
        
        return documentData;
    }
    
    private long processBatch(List<Map<String, Object>> batch, CsvStreamingRequest request, String streamId) {
        // This is a placeholder for actual indexing logic
        // In a real implementation, you would integrate with your search engine (OpenSearch, Elasticsearch, etc.)
        
        logger.debug("Processing batch of {} documents for stream {}", batch.size(), streamId);
        
        // Simulate some processing time and potential failures
        try {
            Thread.sleep(10); // Simulate processing delay
            
            // Simulate 95% success rate
            long successful = (long) (batch.size() * 0.95);
            return successful;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
    
    private void updateProgress(CsvStreamingResponse response, long processed, long indexed, 
                              long failed, long batchCount) {
        response.setRecordsProcessed(processed);
        response.setRecordsIndexed(indexed);
        response.setRecordsFailed(failed);
        response.setBatchCount(batchCount);
        
        // Calculate progress percentage
        if (response.getTotalRecords() > 0) {
            double progress = (processed * 100.0) / response.getTotalRecords();
            response.setProgressPercentage(Math.min(100.0, progress));
        }
        
        // Update processing rate
        long elapsed = System.currentTimeMillis() - 
            response.getStartTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
        if (elapsed > 0) {
            double rate = (processed * 1000.0) / elapsed;
            response.setProcessingRatePerSecond(rate);
        }
    }
    
    private String generateStreamId() {
        return "stream_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
