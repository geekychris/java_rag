package com.example.ragservice.service;

import com.example.ragservice.dto.DirectoryScanRequest;
import com.example.ragservice.dto.DirectoryScanResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DirectoryScanService {
    
    private static final Logger logger = LoggerFactory.getLogger(DirectoryScanService.class);
    
    @Autowired
    private DocumentExtractionService documentExtractionService;
    
    // Store active scan operations
    private final Map<String, DirectoryScanResponse> activeScanOperations = new ConcurrentHashMap<>();
    
    /**
     * Starts a directory scan operation asynchronously
     *
     * @param request the scan request
     * @return the scan response with initial status
     */
    public DirectoryScanResponse startDirectoryScan(DirectoryScanRequest request) {
        String scanId = generateScanId();
        DirectoryScanResponse response = new DirectoryScanResponse(scanId, "STARTED");
        
        activeScanOperations.put(scanId, response);
        
        // Start the scan asynchronously
        CompletableFuture.runAsync(() -> performDirectoryScan(scanId, request));
        
        return response;
    }
    
    /**
     * Gets the status of an active scan operation
     *
     * @param scanId the scan ID
     * @return the current scan status
     */
    public DirectoryScanResponse getScanStatus(String scanId) {
        return activeScanOperations.get(scanId);
    }
    
    /**
     * Lists all active scan operations
     *
     * @return map of active scans
     */
    public Map<String, DirectoryScanResponse> getActiveScanOperations() {
        return new HashMap<>(activeScanOperations);
    }
    
    /**
     * Cancels an active scan operation
     *
     * @param scanId the scan ID
     * @return true if cancelled, false if not found
     */
    public boolean cancelScan(String scanId) {
        DirectoryScanResponse response = activeScanOperations.get(scanId);
        if (response != null && !"COMPLETED".equals(response.getStatus()) && !"FAILED".equals(response.getStatus())) {
            response.setStatus("CANCELLED");
            response.setEndTime(LocalDateTime.now());
            response.setDurationMs(System.currentTimeMillis() - response.getStartTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());
            return true;
        }
        return false;
    }
    
    private void performDirectoryScan(String scanId, DirectoryScanRequest request) {
        DirectoryScanResponse response = activeScanOperations.get(scanId);
        long startTime = System.currentTimeMillis();
        
        try {
            response.setStatus("SCANNING");
            
            Path directoryPath = Paths.get(request.getDirectoryPath());
            if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
                throw new IllegalArgumentException("Invalid directory path: " + request.getDirectoryPath());
            }
            
            // Determine output CSV path
            String outputCsvPath = request.getOutputCsvPath();
            if (outputCsvPath == null || outputCsvPath.trim().isEmpty()) {
                outputCsvPath = directoryPath.toString() + "/extracted_documents_" + scanId + ".csv";
            }
            response.setCsvOutputPath(outputCsvPath);
            
            // Find all supported files
            List<Path> supportedFiles = findSupportedFiles(directoryPath, request);
            response.setTotalFilesFound(supportedFiles.size());
            
            if (supportedFiles.isEmpty()) {
                response.setStatus("COMPLETED");
                response.setEndTime(LocalDateTime.now());
                response.setDurationMs(System.currentTimeMillis() - startTime);
                return;
            }
            
            // Process files and write to CSV
            processFilesToCsv(scanId, supportedFiles, outputCsvPath, request);
            
            // Update final status
            DirectoryScanResponse finalResponse = activeScanOperations.get(scanId);
            if ("CANCELLED".equals(finalResponse.getStatus())) {
                return;
            }
            
            finalResponse.setStatus("COMPLETED");
            finalResponse.setEndTime(LocalDateTime.now());
            finalResponse.setDurationMs(System.currentTimeMillis() - startTime);
            
            logger.info("Directory scan completed. Scan ID: {}, Files processed: {}, Files failed: {}", 
                scanId, finalResponse.getFilesProcessed(), finalResponse.getFilesFailed());
            
        } catch (Exception e) {
            logger.error("Directory scan failed. Scan ID: {}", scanId, e);
            response.setStatus("FAILED");
            response.setEndTime(LocalDateTime.now());
            response.setDurationMs(System.currentTimeMillis() - startTime);
            
            List<String> errors = response.getErrors();
            if (errors == null) {
                errors = new ArrayList<>();
                response.setErrors(errors);
            }
            errors.add("Scan failed: " + e.getMessage());
        }
    }
    
    private List<Path> findSupportedFiles(Path directoryPath, DirectoryScanRequest request) throws IOException {
        List<Path> supportedFiles = new ArrayList<>();
        Set<String> supportedExtensions = new HashSet<>();
        
        // Convert extensions to lowercase for case-insensitive comparison
        for (String ext : request.getSupportedExtensions()) {
            supportedExtensions.add(ext.toLowerCase());
        }
        
        Integer maxFiles = request.getMaxFiles();
        
        // Use breadth-first traversal for better performance with large directory structures
        return findSupportedFilesBreadthFirst(directoryPath, supportedExtensions, request.isRecursive(), maxFiles);
    }
    
    /**
     * Breadth-first directory traversal to find supported files
     * This approach processes directories level by level, which is more memory efficient
     * and provides better performance characteristics for large directory structures
     */
    private List<Path> findSupportedFilesBreadthFirst(Path startDir, Set<String> supportedExtensions, 
                                                     boolean recursive, Integer maxFiles) throws IOException {
        List<Path> supportedFiles = new ArrayList<>();
        Queue<Path> directoriesToProcess = new LinkedList<>();
        Set<Path> visitedDirectories = new HashSet<>();
        
        directoriesToProcess.offer(startDir);
        visitedDirectories.add(startDir);
        
        int fileCount = 0;
        int directoriesProcessed = 0;
        
        while (!directoriesToProcess.isEmpty() && (maxFiles == null || fileCount < maxFiles)) {
            Path currentDir = directoriesToProcess.poll();
            directoriesProcessed++;
            
            // Log progress for large operations
            if (directoriesProcessed % 100 == 0) {
                logger.debug("Processed {} directories, found {} files", directoriesProcessed, fileCount);
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                // First pass: collect files from current directory
                for (Path entry : stream) {
                    if (maxFiles != null && fileCount >= maxFiles) {
                        break;
                    }
                    
                    if (Files.isRegularFile(entry)) {
                        String extension = documentExtractionService.getFileExtension(entry.toString());
                        if (supportedExtensions.contains(extension.toLowerCase())) {
                            supportedFiles.add(entry);
                            fileCount++;
                        }
                    }
                }
                
                // Second pass: collect subdirectories if recursive
                if (recursive && (maxFiles == null || fileCount < maxFiles)) {
                    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(currentDir)) {
                        for (Path entry : dirStream) {
                            if (Files.isDirectory(entry) && !visitedDirectories.contains(entry)) {
                                // Avoid infinite loops with symbolic links
                                try {
                                    Path realPath = entry.toRealPath();
                                    if (!visitedDirectories.contains(realPath)) {
                                        directoriesToProcess.offer(entry);
                                        visitedDirectories.add(entry);
                                        visitedDirectories.add(realPath);
                                    }
                                } catch (IOException e) {
                                    // Handle broken symlinks gracefully
                                    logger.debug("Skipping directory due to IO error: {}", entry, e);
                                }
                            }
                        }
                    }
                }
                
            } catch (IOException e) {
                logger.warn("Failed to read directory: {}", currentDir, e);
                // Continue with other directories instead of failing entirely
            }
        }
        
        logger.info("Breadth-first scan completed: {} directories processed, {} files found", 
                   directoriesProcessed, fileCount);
        
        return supportedFiles;
    }
    
    private void processFilesToCsv(String scanId, List<Path> files, String outputCsvPath, DirectoryScanRequest request) {
        DirectoryScanResponse response = activeScanOperations.get(scanId);
        
        try (FileWriter fileWriter = new FileWriter(outputCsvPath);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                 .withHeader("path", "file_name", "file_path", "file_size", "content_type", "text", "metadata"))) {
            
            AtomicInteger processed = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            AtomicLong totalSize = new AtomicLong(0);
            
            Set<String> processedExtensions = new HashSet<>();
            List<String> errors = new ArrayList<>();
            
            for (Path filePath : files) {
                if ("CANCELLED".equals(response.getStatus())) {
                    break;
                }
                
                try {
                    DocumentExtractionService.ExtractedDocument document = 
                        documentExtractionService.extractText(filePath);
                    
                    // Add file extension to processed set
                    String extension = documentExtractionService.getFileExtension(filePath.toString());
                    processedExtensions.add(extension.toLowerCase());
                    
                    // Write to CSV
                    Map<String, String> metadata = document.getMetadata();
                    String metadataJson = convertMetadataToJson(metadata);
                    
                    csvPrinter.printRecord(
                        filePath.toString(),
                        metadata.get("file_name"),
                        metadata.get("file_path"),
                        metadata.get("file_size"),
                        metadata.get("content_type"),
                        document.getText(),
                        metadataJson
                    );
                    
                    // Flush immediately to enable real-time progress viewing
                    csvPrinter.flush();
                    
                    totalSize.addAndGet(Long.parseLong(metadata.get("file_size")));
                    processed.incrementAndGet();
                    
                    logger.info("Successfully processed file {}/{}: {}", 
                               processed.get(), files.size(), filePath.getFileName());
                    
                } catch (Exception e) {
                    logger.warn("Failed to extract text from file: {}", filePath, e);
                    failed.incrementAndGet();
                    errors.add("Failed to process " + filePath + ": " + e.getMessage());
                }
                
                // Update progress
                response.setFilesProcessed(processed.get());
                response.setFilesFailed(failed.get());
                response.setProcessedExtensions(new ArrayList<>(processedExtensions));
                
                if (errors.size() <= 10) { // Limit error list size
                    response.setErrors(errors);
                }
            }
            
            csvPrinter.flush();
            
            logger.info("CSV file created: {}, Total files processed: {}, Total size: {} bytes", 
                outputCsvPath, processed.get(), totalSize.get());
            
        } catch (IOException e) {
            logger.error("Failed to write CSV file: {}", outputCsvPath, e);
            response.setStatus("FAILED");
            
            List<String> errors = response.getErrors();
            if (errors == null) {
                errors = new ArrayList<>();
                response.setErrors(errors);
            }
            errors.add("Failed to write CSV file: " + e.getMessage());
        }
    }
    
    private String convertMetadataToJson(Map<String, String> metadata) {
        // Simple JSON conversion for metadata
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":\"");
            json.append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String generateScanId() {
        return "scan_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
