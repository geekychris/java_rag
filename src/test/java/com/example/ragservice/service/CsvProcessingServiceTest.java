package com.example.ragservice.service;

import com.example.ragservice.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvProcessingServiceTest {

    private CsvProcessingService csvProcessingService;

    @BeforeEach
    void setUp() {
        csvProcessingService = new CsvProcessingService();
    }

    @Test
    void testParseCsvToDocuments_ValidCsv() {
        // Given
        String csvContent = """
            title,content,category
            "Document 1","This is the first document","Technology"
            "Document 2","This is the second document","Science"
            """;
        String contentColumnName = "content";
        String source = "test-source";

        // When
        List<Document> documents = csvProcessingService.parseCsvToDocuments(csvContent, contentColumnName, source);

        // Then
        assertEquals(2, documents.size());
        
        Document doc1 = documents.get(0);
        assertEquals("This is the first document", doc1.getContent());
        assertEquals("Document 1", doc1.getMetadata().get("title"));
        assertEquals("Technology", doc1.getMetadata().get("category"));
        assertEquals(1, doc1.getMetadata().get("csv_record_number"));
        assertEquals(source, doc1.getSource());
        
        Document doc2 = documents.get(1);
        assertEquals("This is the second document", doc2.getContent());
        assertEquals("Document 2", doc2.getMetadata().get("title"));
        assertEquals("Science", doc2.getMetadata().get("category"));
        assertEquals(2, doc2.getMetadata().get("csv_record_number"));
    }

    @Test
    void testParseCsvToDocuments_InvalidContentColumn() {
        // Given
        String csvContent = """
            title,description
            "Document 1","This is a description"
            """;
        String contentColumnName = "content"; // This column doesn't exist

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            csvProcessingService.parseCsvToDocuments(csvContent, contentColumnName, null)
        );
        
        assertTrue(exception.getMessage().contains("Content column 'content' not found"));
    }

    @Test
    void testParseCsvToDocuments_EmptyContent() {
        // Given
        String csvContent = """
            title,content
            "Document 1","This is content"
            "Document 2",""
            "Document 3","More content"
            """;
        String contentColumnName = "content";

        // When
        List<Document> documents = csvProcessingService.parseCsvToDocuments(csvContent, contentColumnName, null);

        // Then
        assertEquals(2, documents.size()); // Empty content row should be skipped
        assertEquals("This is content", documents.get(0).getContent());
        assertEquals("More content", documents.get(1).getContent());
    }

    @Test
    void testIsValidCsv_ValidFormat() {
        // Given
        String validCsv = """
            header1,header2
            "value1","value2"
            """;

        // When
        boolean isValid = csvProcessingService.isValidCsv(validCsv);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testIsValidCsv_InvalidFormat() {
        // Given
        String invalidCsv = "This is not a CSV";

        // When
        boolean isValid = csvProcessingService.isValidCsv(invalidCsv);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsValidCsv_NoData() {
        // Given
        String headerOnlyCsv = "header1,header2";

        // When
        boolean isValid = csvProcessingService.isValidCsv(headerOnlyCsv);

        // Then
        assertFalse(isValid); // No data rows
    }

    @Test
    void testGetCsvHeaders() {
        // Given
        String csvContent = """
            title,content,category,author
            "Doc1","Content1","Tech","John"
            """;

        // When
        List<String> headers = csvProcessingService.getCsvHeaders(csvContent);

        // Then
        assertEquals(4, headers.size());
        assertTrue(headers.contains("title"));
        assertTrue(headers.contains("content"));
        assertTrue(headers.contains("category"));
        assertTrue(headers.contains("author"));
    }

    @Test
    void testCountCsvRecords() {
        // Given
        String csvContent = """
            title,content
            "Doc1","Content1"
            "Doc2","Content2"
            "Doc3","Content3"
            """;

        // When
        int count = csvProcessingService.countCsvRecords(csvContent);

        // Then
        assertEquals(3, count);
    }

    @Test
    void testCountCsvRecords_HeaderOnly() {
        // Given
        String csvContent = "title,content";

        // When
        int count = csvProcessingService.countCsvRecords(csvContent);

        // Then
        assertEquals(0, count);
    }

    @Test
    void testParseCsvToDocuments_WithQuotesAndCommas() {
        // Given
        String csvContent = """
            title,content
            "Document with, comma","Content with ""quotes"" and, comma"
            """;
        String contentColumnName = "content";

        // When
        List<Document> documents = csvProcessingService.parseCsvToDocuments(csvContent, contentColumnName, null);

        // Then
        assertEquals(1, documents.size());
        Document doc = documents.get(0);
        assertEquals("Content with \"quotes\" and, comma", doc.getContent());
        assertEquals("Document with, comma", doc.getMetadata().get("title"));
    }
}
