package com.example.ragservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/file-viewer")
@CrossOrigin(origins = "http://localhost:3000")
public class FileViewerController {

    private static final Logger logger = LoggerFactory.getLogger(FileViewerController.class);

    /**
     * Serves file content for viewing in the browser
     * 
     * @param encodedPath Base64 encoded file path for security
     * @return File content with appropriate headers
     */
    @GetMapping("/content")
    public ResponseEntity<?> getFileContent(@RequestParam String path) {
        try {
            // Decode the base64 encoded path
            String decodedPath = new String(Base64.getDecoder().decode(path));
            Path filePath = Paths.get(decodedPath);
            
            logger.info("Serving file content for: {}", decodedPath);
            
            // Security checks
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            if (!Files.isRegularFile(filePath)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Path is not a regular file"));
            }
            
            // Check if file is readable
            if (!Files.isReadable(filePath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "File is not readable"));
            }
            
            // Get file info
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();
            String mimeType = Files.probeContentType(filePath);
            
            // Default to text/plain if MIME type cannot be determined
            if (mimeType == null) {
                mimeType = "text/plain";
            }
            
            logger.debug("File info - Name: {}, Size: {}, MIME: {}", fileName, fileSize, mimeType);
            
            // For text files, return content as JSON for easier handling in UI
            if (mimeType.startsWith("text/") || mimeType.equals("application/pdf")) {
                try {
                    String content;
                    if (mimeType.equals("application/pdf")) {
                        // For PDFs, we'll return metadata only since rendering PDF content as text
                        // in the browser isn't very useful
                        content = "[PDF Content - " + fileSize + " bytes]";
                    } else {
                        content = Files.readString(filePath);
                        // Limit content size to prevent memory issues
                        if (content.length() > 50000) {
                            content = content.substring(0, 50000) + "\n\n... [Content truncated at 50,000 characters]";
                        }
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("fileName", fileName);
                    response.put("filePath", decodedPath);
                    response.put("fileSize", fileSize);
                    response.put("mimeType", mimeType);
                    response.put("content", content);
                    
                    return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
                        
                } catch (IOException e) {
                    logger.error("Failed to read file content: {}", decodedPath, e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to read file content: " + e.getMessage()));
                }
            } else {
                // For binary files, serve as downloadable resource
                Resource resource = new FileSystemResource(filePath);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid base64 encoded path: {}", path);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid path encoding"));
        } catch (Exception e) {
            logger.error("Error serving file content for path: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Gets file information without content
     * 
     * @param encodedPath Base64 encoded file path
     * @return File metadata
     */
    @GetMapping("/info")
    public ResponseEntity<?> getFileInfo(@RequestParam String path) {
        try {
            String decodedPath = new String(Base64.getDecoder().decode(path));
            Path filePath = Paths.get(decodedPath);
            
            logger.info("Getting file info for: {}", decodedPath);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            if (!Files.isRegularFile(filePath)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Path is not a regular file"));
            }
            
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();
            String mimeType = Files.probeContentType(filePath);
            
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("filePath", decodedPath);
            response.put("fileSize", fileSize);
            response.put("mimeType", mimeType);
            response.put("lastModified", Files.getLastModifiedTime(filePath).toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid base64 encoded path: {}", path);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid path encoding"));
        } catch (Exception e) {
            logger.error("Error getting file info for path: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}
