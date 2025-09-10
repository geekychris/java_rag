package com.example.ragservice.service;

import com.example.ragservice.model.Document;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CsvProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvProcessingService.class);
    
    /**
     * Parse CSV content and convert to Document objects
     * @param csvContent The CSV content as string
     * @param contentColumnName The name of the column containing the main content
     * @param source Optional source identifier for the documents
     * @return List of Document objects
     */
    public List<Document> parseCsvToDocuments(String csvContent, String contentColumnName, String source) {
        return parseCsvToDocuments(csvContent, contentColumnName, null, source);
    }
    
    /**
     * Parse CSV content and convert to Document objects with optional document ID column
     * @param csvContent The CSV content as string
     * @param contentColumnName The name of the column containing the main content
     * @param docIdColumnName The name of the column containing document IDs (optional)
     * @param source Optional source identifier for the documents
     * @return List of Document objects
     */
    public List<Document> parseCsvToDocuments(String csvContent, String contentColumnName, String docIdColumnName, String source) {
        List<Document> documents = new ArrayList<>();
        
        try (StringReader stringReader = new StringReader(csvContent);
             CSVParser csvParser = CSVFormat.DEFAULT
                 .withFirstRecordAsHeader()
                 .withIgnoreHeaderCase()
                 .withTrim()
                 .parse(stringReader)) {
            
            logger.info("Parsing CSV with headers: {}", csvParser.getHeaderNames());
            
            // Validate that the content column exists
            if (!csvParser.getHeaderNames().contains(contentColumnName)) {
                throw new IllegalArgumentException("Content column '" + contentColumnName + "' not found in CSV headers: " + csvParser.getHeaderNames());
            }
            
            // Validate that the document ID column exists if specified
            boolean hasDocIdColumn = docIdColumnName != null && !docIdColumnName.trim().isEmpty();
            if (hasDocIdColumn && !csvParser.getHeaderNames().contains(docIdColumnName)) {
                logger.warn("Document ID column '{}' not found in CSV headers: {}. Will generate UUIDs instead.", docIdColumnName, csvParser.getHeaderNames());
                hasDocIdColumn = false;
            }
            
            int recordCount = 0;
            for (CSVRecord record : csvParser) {
                try {
                    String content = record.get(contentColumnName);
                    if (content == null || content.trim().isEmpty()) {
                        logger.warn("Skipping record {} with empty content", recordCount + 1);
                        continue;
                    }
                    
                    // Determine document ID
                    String documentId;
                    if (hasDocIdColumn) {
                        String csvDocId = record.get(docIdColumnName);
                        if (csvDocId != null && !csvDocId.trim().isEmpty()) {
                            documentId = csvDocId.trim();
                        } else {
                            logger.warn("Empty document ID in record {}, generating UUID", recordCount + 1);
                            documentId = UUID.randomUUID().toString();
                        }
                    } else {
                        documentId = UUID.randomUUID().toString();
                    }
                    
                    // Create metadata from all other columns (excluding content and doc_id)
                    Map<String, Object> metadata = new HashMap<>();
                    for (String header : csvParser.getHeaderNames()) {
                        if (!header.equals(contentColumnName) && (!hasDocIdColumn || !header.equals(docIdColumnName))) {
                            String value = record.get(header);
                            if (value != null && !value.trim().isEmpty()) {
                                metadata.put(header, value.trim());
                            }
                        }
                    }
                    
                    // Add record number and original doc_id for reference
                    metadata.put("csv_record_number", recordCount + 1);
                    if (hasDocIdColumn) {
                        metadata.put("original_doc_id", documentId);
                    }
                    
                    Document document = new Document(
                        documentId,
                        content.trim(),
                        metadata
                    );
                    
                    if (source != null && !source.trim().isEmpty()) {
                        document.setSource(source.trim());
                    }
                    
                    documents.add(document);
                    recordCount++;
                    
                } catch (Exception e) {
                    logger.error("Error processing CSV record {}: {}", recordCount + 1, e.getMessage());
                    // Continue processing other records
                }
            }
            
            logger.info("Successfully parsed {} documents from CSV", documents.size());
            
        } catch (IOException e) {
            logger.error("Error parsing CSV content", e);
            throw new RuntimeException("Failed to parse CSV content", e);
        }
        
        return documents;
    }
    
    /**
     * Validate CSV content format
     * @param csvContent The CSV content to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidCsv(String csvContent) {
        try (StringReader stringReader = new StringReader(csvContent);
             CSVParser csvParser = CSVFormat.DEFAULT
                 .withFirstRecordAsHeader()
                 .withIgnoreHeaderCase()
                 .withTrim()
                 .parse(stringReader)) {
            
            // Check if we have headers
            List<String> headers = csvParser.getHeaderNames();
            if (headers.isEmpty()) {
                return false;
            }
            
            // Check if we have at least one record
            for (CSVRecord record : csvParser) {
                return true; // At least one record exists
            }
            
            return false; // No records found
            
        } catch (Exception e) {
            logger.debug("CSV validation failed", e);
            return false;
        }
    }
    
    /**
     * Get CSV headers from the content
     * @param csvContent The CSV content
     * @return List of header names
     */
    public List<String> getCsvHeaders(String csvContent) {
        try (StringReader stringReader = new StringReader(csvContent);
             CSVParser csvParser = CSVFormat.DEFAULT
                 .withFirstRecordAsHeader()
                 .withIgnoreHeaderCase()
                 .withTrim()
                 .parse(stringReader)) {
            
            return new ArrayList<>(csvParser.getHeaderNames());
            
        } catch (Exception e) {
            logger.error("Error extracting CSV headers", e);
            throw new RuntimeException("Failed to extract CSV headers", e);
        }
    }
    
    /**
     * Count the number of records in CSV content
     * @param csvContent The CSV content
     * @return Number of data records (excluding header)
     */
    public int countCsvRecords(String csvContent) {
        try (StringReader stringReader = new StringReader(csvContent);
             CSVParser csvParser = CSVFormat.DEFAULT
                 .withFirstRecordAsHeader()
                 .parse(stringReader)) {
            
            int count = 0;
            for (CSVRecord ignored : csvParser) {
                count++;
            }
            return count;
            
        } catch (Exception e) {
            logger.error("Error counting CSV records", e);
            return 0;
        }
    }
}
