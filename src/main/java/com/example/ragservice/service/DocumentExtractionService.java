package com.example.ragservice.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.time.Duration;

@Service
public class DocumentExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentExtractionService.class);
    
    private final Tika tika;
    private final AutoDetectParser parser;
    private final ExecutorService executorService;
    
    // Default timeout for document extraction (2 minutes)
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(2);
    
    // Supported file extensions
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        "pdf", "txt", "docx", "doc", "rtf", "html", "htm", "xml", 
        "odt", "ods", "odp", "pptx", "ppt", "xlsx", "xls", "csv"
    );
    
    public DocumentExtractionService() {
        this.tika = new Tika();
        this.parser = new AutoDetectParser();
        // Set maximum string length to handle large documents
        this.tika.setMaxStringLength(-1); // No limit
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Extracts text content from a file with default timeout
     *
     * @param filePath the path to the file
     * @return ExtractedDocument containing text and metadata
     * @throws DocumentExtractionException if extraction fails
     */
    public ExtractedDocument extractText(Path filePath) throws DocumentExtractionException {
        return extractText(filePath, DEFAULT_TIMEOUT);
    }
    
    /**
     * Extracts text content from a file with specified timeout
     *
     * @param filePath the path to the file
     * @param timeout the maximum time to spend on extraction
     * @return ExtractedDocument containing text and metadata
     * @throws DocumentExtractionException if extraction fails
     */
    public ExtractedDocument extractText(Path filePath, Duration timeout) throws DocumentExtractionException {
        if (!Files.exists(filePath)) {
            throw new DocumentExtractionException("File does not exist: " + filePath);
        }
        
        if (!Files.isRegularFile(filePath)) {
            throw new DocumentExtractionException("Path is not a regular file: " + filePath);
        }
        
        String extension = getFileExtension(filePath.toString());
        if (!isSupportedExtension(extension)) {
            throw new DocumentExtractionException("Unsupported file extension: " + extension);
        }
        
        try {
            // Create a callable for the extraction task
            Callable<ExtractedDocument> extractionTask = () -> {
                // Special handling for plain text files
                if ("txt".equalsIgnoreCase(extension)) {
                    return extractPlainText(filePath);
                }
                
                // Use Tika for other formats
                return extractWithTika(filePath);
            };
            
            // Execute with timeout
            Future<ExtractedDocument> future = executorService.submit(extractionTask);
            
            try {
                return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true); // Interrupt the extraction task
                logger.warn("Document extraction timed out after {} seconds for file: {}", 
                           timeout.getSeconds(), filePath);
                throw new DocumentExtractionException(
                    String.format("Document extraction timed out after %d seconds for file: %s", 
                                 timeout.getSeconds(), filePath), e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof DocumentExtractionException) {
                    throw (DocumentExtractionException) cause;
                }
                throw new DocumentExtractionException("Failed to extract text from file: " + filePath, cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DocumentExtractionException("Document extraction was interrupted for file: " + filePath, e);
            }
            
        } catch (DocumentExtractionException e) {
            throw e; // Re-throw our custom exception
        } catch (Exception e) {
            logger.error("Failed to extract text from file: {}", filePath, e);
            throw new DocumentExtractionException("Failed to extract text from file: " + filePath, e);
        }
    }
    
    /**
     * Checks if a file extension is supported
     *
     * @param extension the file extension
     * @return true if supported, false otherwise
     */
    public boolean isSupportedExtension(String extension) {
        if (extension == null) {
            return false;
        }
        return SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * Gets the file extension from a filename
     *
     * @param fileName the filename
     * @return the extension or empty string if none
     */
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
    
    /**
     * Gets supported file extensions
     *
     * @return set of supported extensions
     */
    public Set<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
    
    private ExtractedDocument extractPlainText(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("file_name", filePath.getFileName().toString());
        metadata.put("file_path", filePath.toString());
        metadata.put("file_size", String.valueOf(Files.size(filePath)));
        metadata.put("content_type", "text/plain");
        metadata.put("extraction_method", "direct_read");
        
        return new ExtractedDocument(content, metadata);
    }
    
    private ExtractedDocument extractWithTika(Path filePath) throws IOException, TikaException, SAXException {
        File file = filePath.toFile();
        
        // Create metadata object to capture document properties
        Metadata metadata = new Metadata();
        
        // Create content handler with no limit on string length
        BodyContentHandler handler = new BodyContentHandler(-1);
        
        // Configure Tika to disable OCR for PDFs (avoids expensive OCR on scanned images)
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.NO_OCR);
        ParseContext parseContext = new ParseContext();
        parseContext.set(PDFParserConfig.class, pdfConfig);
        
        try (FileInputStream inputStream = new FileInputStream(file)) {
            parser.parse(inputStream, handler, metadata, parseContext);
        }
        
        String extractedText = handler.toString().trim();
        
        // Convert Tika metadata to our format
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("file_name", filePath.getFileName().toString());
        metadataMap.put("file_path", filePath.toString());
        metadataMap.put("file_size", String.valueOf(Files.size(filePath)));
        
        // Add Tika metadata
        for (String name : metadata.names()) {
            metadataMap.put(name, metadata.get(name));
        }
        
        metadataMap.put("extraction_method", "apache_tika");
        metadataMap.put("text_length", String.valueOf(extractedText.length()));
        
        return new ExtractedDocument(extractedText, metadataMap);
    }
    
    /**
     * Container for extracted document content and metadata
     */
    public static class ExtractedDocument {
        private final String text;
        private final Map<String, String> metadata;
        
        public ExtractedDocument(String text, Map<String, String> metadata) {
            this.text = text;
            this.metadata = metadata;
        }
        
        public String getText() {
            return text;
        }
        
        public Map<String, String> getMetadata() {
            return metadata;
        }
        
        public boolean isEmpty() {
            return text == null || text.trim().isEmpty();
        }
    }
    
    /**
     * Exception thrown when document extraction fails
     */
    public static class DocumentExtractionException extends Exception {
        public DocumentExtractionException(String message) {
            super(message);
        }
        
        public DocumentExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
