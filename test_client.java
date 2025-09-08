import com.example.ragservice.client.RagClient;
import com.example.ragservice.client.RagClientImpl;
import com.example.ragservice.client.config.RagClientConfig;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.dto.SearchResponse;

import java.time.Duration;

public class test_client {
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing RAG Client Library ===\n");
            
            // Create client with longer timeouts
            RagClientConfig config = RagClientConfig.builder()
                .baseUrl("http://localhost:8080")
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
            
            try (RagClient client = new RagClientImpl(config)) {
                
                // Test 1: Health check
                System.out.println("1. Testing health check...");
                boolean healthy = client.isHealthy();
                System.out.println("   Service healthy: " + healthy);
                
                if (!healthy) {
                    System.out.println("   Service is not healthy, exiting.");
                    return;
                }
                
                // Test 2: Service info
                System.out.println("\n2. Getting service info...");
                try {
                    String info = client.getServiceInfo();
                    System.out.println("   Service info: " + info);
                } catch (Exception e) {
                    System.out.println("   Service info not available: " + e.getMessage());
                }
                
                // Test 3: Upload CSV data
                System.out.println("\n3. Testing CSV upload...");
                String csvData = "title,content,category\n" +
                    "Test Article 1,\"This is a test article about artificial intelligence and machine learning.\",Technology\n" +
                    "Test Article 2,\"This article discusses natural language processing and deep learning.\",Technology";
                
                CsvUploadRequest csvRequest = CsvUploadRequest.builder()
                    .csvContent(csvData)
                    .indexName("client-test-index")
                    .contentColumnName("content")
                    .source("client-library-test")
                    .build();
                
                CsvUploadResponse uploadResponse = client.uploadCsv(csvRequest);
                System.out.println("   Upload successful: " + uploadResponse.isSuccess());
                System.out.println("   Documents indexed: " + uploadResponse.getDocumentsIndexed());
                System.out.println("   Index name: " + uploadResponse.getIndexName());
                
                // Wait a bit for indexing
                System.out.println("   Waiting for indexing to complete...");
                Thread.sleep(3000);
                
                // Test 4: Search
                System.out.println("\n4. Testing search...");
                SearchResponse searchResponse = client.search("machine learning", "client-test-index");
                System.out.println("   Search successful: " + searchResponse.isSuccess());
                System.out.println("   Total results: " + searchResponse.getTotalResults());
                System.out.println("   Query: " + searchResponse.getQuery());
                
                if (searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
                    System.out.println("   Results:");
                    searchResponse.getResults().forEach(result -> {
                        System.out.println("     - Score: " + String.format("%.3f", result.getScore()));
                        System.out.println("       Content: " + result.getDocument().getContent());
                        System.out.println("       Metadata: " + result.getDocument().getMetadata());
                    });
                }
                
                // Test 5: Advanced search
                System.out.println("\n5. Testing advanced search...");
                SearchResponse advancedResponse = client.search("deep learning", "client-test-index", 5, 0.1);
                System.out.println("   Advanced search results: " + advancedResponse.getTotalResults());
                
                System.out.println("\n=== All tests completed successfully! ===");
                
            }
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
