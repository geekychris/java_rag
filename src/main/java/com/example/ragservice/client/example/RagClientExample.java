package com.example.ragservice.client.example;

import com.example.ragservice.client.RagClient;
import com.example.ragservice.client.RagClientImpl;
import com.example.ragservice.client.config.RagClientConfig;
import com.example.ragservice.client.dto.SearchRequest;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.client.exception.RagClientException;
import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.model.Document;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Example demonstrating how to use the RAG client library.
 * 
 * This example shows:
 * - Creating a client with custom configuration
 * - Uploading CSV data
 * - Performing searches
 * - Uploading individual documents
 * - Error handling
 */
public class RagClientExample {
    
    public static void main(String[] args) {
        // Example 1: Create client with default configuration
        System.out.println("=== RAG Client Library Example ===\n");
        
        try (RagClient defaultClient = RagClientImpl.create()) {
            System.out.println("1. Testing default client connection...");
            if (defaultClient.isHealthy()) {
                System.out.println("✓ RAG service is healthy");
            } else {
                System.out.println("✗ RAG service is not reachable");
                return;
            }
        }
        
        // Example 2: Create client with custom configuration
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl("http://localhost:8080")
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .retryDelay(Duration.ofSeconds(1))
                .build();
        
        try (RagClient ragClient = new RagClientImpl(config)) {
            
            // Example 3: Upload CSV data
            System.out.println("\n2. Uploading CSV data...");
            String csvData = """
                title,content,category,author,publication_date
                Introduction to Machine Learning,"Machine Learning is a subset of artificial intelligence that enables computers to learn and make decisions without being explicitly programmed.",Technology,Alice Johnson,2024-01-15
                Deep Learning Fundamentals,"Deep learning is part of machine learning methods based on artificial neural networks with representation learning.",Technology,Bob Smith,2024-01-20
                Natural Language Processing Guide,"NLP is a subfield of linguistics and artificial intelligence concerned with interactions between computers and human language.",Technology,Carol Davis,2024-01-25
                """;
            
            CsvUploadRequest csvRequest = CsvUploadRequest.builder()
                    .csvContent(csvData)
                    .indexName("example-tech-articles")
                    .contentColumnName("content")
                    .source("client-library-example")
                    .build();
            
            CsvUploadResponse csvResponse = ragClient.uploadCsv(csvRequest);
            System.out.println("✓ Successfully uploaded " + csvResponse.getDocumentsIndexed() + " documents");
            System.out.println("  Index: " + csvResponse.getIndexName());
            System.out.println("  Headers: " + csvResponse.getCsvHeaders());
            
            // Wait for indexing to complete
            System.out.println("  Waiting for indexing...");
            Thread.sleep(3000);
            
            // Example 4: Perform searches
            System.out.println("\n3. Performing searches...");
            
            // Simple search
            SearchResponse response1 = ragClient.search("machine learning", "example-tech-articles");
            System.out.println("✓ Simple search found " + response1.getTotalResults() + " results");
            printSearchResults(response1, "Simple Search");
            
            // Advanced search with parameters
            SearchResponse response2 = ragClient.search("neural networks", "example-tech-articles", 2, 0.3);
            System.out.println("✓ Advanced search found " + response2.getTotalResults() + " results");
            printSearchResults(response2, "Advanced Search");
            
            // Search with request object
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("artificial intelligence")
                    .indexName("example-tech-articles")
                    .maxResults(5)
                    .minScore(0.1)
                    .vectorSearch() // or .hybridSearch()
                    .build();
            
            SearchResponse response3 = ragClient.search(searchRequest);
            System.out.println("✓ Request object search found " + response3.getTotalResults() + " results");
            printSearchResults(response3, "Request Object Search");
            
            // Example 5: Upload individual documents
            System.out.println("\n4. Uploading individual documents...");
            
            Document document1 = new Document();
            document1.setId("example-doc-1");
            document1.setContent("Cloud computing is the delivery of computing services over the internet, including storage, processing power, and software applications.");
            document1.setMetadata(Map.of(
                    "title", "Cloud Computing Overview",
                    "author", "Technical Team",
                    "category", "Technology",
                    "publication_date", "2024-02-01"
            ));
            document1.setSource("client-library-example");
            
            String docId1 = ragClient.uploadDocument(document1, "example-individual-docs");
            System.out.println("✓ Uploaded document: " + docId1);
            
            // Example 6: Upload multiple documents
            List<Document> documents = Arrays.asList(
                    createExampleDocument("doc-2", "Cybersecurity best practices", "Information security"),
                    createExampleDocument("doc-3", "DevOps methodologies", "Software development"),
                    createExampleDocument("doc-4", "Data analytics techniques", "Data science")
            );
            
            List<String> docIds = ragClient.uploadDocuments(documents, "example-batch-docs");
            System.out.println("✓ Uploaded batch documents: " + docIds);
            
            // Example 7: Get service information
            System.out.println("\n5. Service information:");
            try {
                String serviceInfo = ragClient.getServiceInfo();
                System.out.println("✓ Service info: " + serviceInfo);
            } catch (RagClientException e) {
                System.out.println("ℹ Service info not available: " + e.getMessage());
            }
            
            System.out.println("\n=== Example completed successfully! ===");
            
        } catch (RagClientException e) {
            System.err.println("RAG Client Error: " + e.getMessage());
            if (e.hasStatusCode()) {
                System.err.println("HTTP Status: " + e.getStatusCode());
                System.err.println("Error Body: " + e.getErrorBody());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Example interrupted");
        }
    }
    
    private static void printSearchResults(SearchResponse response, String searchType) {
        System.out.println("  " + searchType + " Results:");
        response.getResults().forEach(result -> {
            String title = (String) result.getDocument().getMetadata().get("title");
            System.out.println("    • " + title + " (Score: " + String.format("%.3f", result.getScore()) + ")");
            System.out.println("      " + truncate(result.getDocument().getContent(), 80));
        });
    }
    
    private static Document createExampleDocument(String id, String title, String category) {
        Document doc = new Document();
        doc.setId(id);
        doc.setContent("This is example content for " + title + " related to " + category + ".");
        doc.setMetadata(Map.of(
                "title", title,
                "category", category,
                "author", "Example Author",
                "publication_date", "2024-02-10"
        ));
        doc.setSource("client-library-batch-example");
        return doc;
    }
    
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
