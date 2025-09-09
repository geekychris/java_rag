package com.example.ragservice;

import com.example.ragservice.client.RagClient;
import com.example.ragservice.client.RagClientImpl;
import com.example.ragservice.client.config.RagClientConfig;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.client.dto.SearchRequest;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.SummarizationRequest;
import com.example.ragservice.dto.SummarizationResponse;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for summarization functionality using the RAG client library.
 * 
 * This test requires the RAG service to be running with OpenSearch and Ollama dependencies.
 * Run this test with: mvn test -Dtest=SummarizationIntegrationTest -Dintegration.test.enabled=true
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SummarizationIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SummarizationIntegrationTest.class);
    
    private static final String SERVICE_URL = System.getProperty("rag.service.url", "http://localhost:8080");
    private static final String TEST_INDEX = "summarization-integration-test";
    
    private static RagClient client;
    
    // Sample CSV data for testing with repeated keywords for better matching
    private static final String TEST_CSV_DATA = 
        "title,content,category,author\n" +
        "\"Machine Learning Essentials\",\"Machine learning algorithms enable computers to learn patterns from data automatically. Machine learning techniques include supervised learning, unsupervised learning, and reinforcement learning. Machine learning is essential for artificial intelligence applications and data science projects.\",\"AI\",\"Dr. Alice Johnson\"\n" +
        "\"Neural Networks Tutorial\",\"Neural networks are machine learning models inspired by biological neural networks. These artificial neural networks process information through interconnected nodes. Deep neural networks with multiple layers are powerful for machine learning tasks and pattern recognition applications.\",\"AI\",\"Prof. Bob Smith\"\n" +
        "\"Python Programming Guide\",\"Python programming language is popular for software development, data analysis, and machine learning projects. Python syntax is simple making Python programming accessible to beginners. Python libraries and frameworks make Python ideal for programming automation and development tasks.\",\"Programming\",\"Sarah Wilson\"\n" +
        "\"JavaScript Development\",\"JavaScript programming is essential for web development and interactive website creation. JavaScript code enables dynamic user interfaces and programming interactive features. Modern JavaScript frameworks make programming web applications more efficient and powerful for development teams.\",\"Programming\",\"Mike Chen\"\n" +
        "\"Artificial Intelligence Overview\",\"Artificial intelligence systems can perform tasks that typically require human intelligence. AI applications include machine learning, natural language processing, computer vision, and robotics. Modern artificial intelligence uses neural networks and machine learning algorithms to solve complex problems.\",\"AI\",\"Dr. Emily Davis\"\n" +
        "\"Data Science Methods\",\"Data science combines programming, statistics, and domain expertise to extract insights from data. Data scientists use programming languages like Python and machine learning tools to analyze datasets. Data science applications include business intelligence, predictive analytics, and artificial intelligence development.\",\"Data\",\"Alex Rodriguez\"";
    
    @BeforeAll
    static void setUp() {
        // Skip integration tests unless explicitly enabled
        String integrationTestEnabled = System.getProperty("integration.test.enabled", "false");
        Assumptions.assumeTrue("true".equalsIgnoreCase(integrationTestEnabled), 
            "Integration tests are disabled. Enable with -Dintegration.test.enabled=true");
        
        logger.info("Setting up integration test with service URL: {}", SERVICE_URL);
        
        // Create client with longer timeouts for integration testing
        RagClientConfig config = RagClientConfig.builder()
            .baseUrl(SERVICE_URL)
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofMinutes(2))  // Summarization can take time
            .maxRetries(3)
            .build();
        
        client = new RagClientImpl(config);
        
        // Verify service is healthy
        assertTrue(client.isHealthy(), "RAG service should be healthy");
        logger.info("RAG service health check passed");
    }
    
    @AfterAll
    static void tearDown() {
        if (client != null) {
            try {
                // Clean up test index (ignore errors)
                logger.info("Cleaning up test index: {}", TEST_INDEX);
                // Note: The client doesn't have a delete index method, so we'll leave it
                // The bash script cleanup will handle this
            } finally {
                client.close();
                logger.info("RAG client closed");
            }
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Upload test data for summarization testing")
    void testUploadTestData() throws Exception {
        logger.info("Uploading test data to index: {}", TEST_INDEX);
        
        CsvUploadRequest request = CsvUploadRequest.builder()
            .csvContent(TEST_CSV_DATA)
            .indexName(TEST_INDEX)
            .contentColumnName("content")
            .source("integration-test")
            .build();
        
        CsvUploadResponse response = client.uploadCsv(request);
        
        assertNotNull(response, "Upload response should not be null");
        assertTrue(response.isSuccess(), "Upload should be successful");
        assertTrue(response.getDocumentsIndexed() > 0, "Should have indexed some documents");
        
        logger.info("Successfully uploaded {} documents", response.getDocumentsIndexed());
        
        // Wait for indexing to complete
        Thread.sleep(8000);
    }
    
    @Test
    @Order(2)
    @DisplayName("Test basic search functionality before summarization")
    void testBasicSearch() throws Exception {
        logger.info("Testing basic search functionality");
        
        SearchRequest request = SearchRequest.builder()
            .query("machine learning")
            .indexName(TEST_INDEX)
            .maxResults(5)
            .minScore(0.01)
            .build();
        
        SearchResponse response = client.search(request);
        
        assertNotNull(response, "Search response should not be null");
        assertTrue(response.isSuccess(), "Search should be successful");
        assertTrue(response.getTotalResults() > 0, "Should find some results");
        assertNotNull(response.getResults(), "Results list should not be null");
        
        logger.info("Search found {} results", response.getTotalResults());
        
        // Log first result for verification
        if (!response.getResults().isEmpty()) {
            var firstResult = response.getResults().get(0);
            logger.info("First result score: {}, content preview: {}", 
                firstResult.getScore(), 
                firstResult.getDocument().getContent().substring(0, Math.min(100, firstResult.getDocument().getContent().length())));
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("Test search and summarize functionality")
    void testSearchAndSummarize() throws Exception {
        logger.info("Testing search and summarize functionality");
        
        String query = "machine learning neural networks";
        
        Map<String, Object> result = client.searchAndSummarize(query, TEST_INDEX, 5, 0.01);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(Boolean.TRUE, result.get("success"), "Operation should be successful");
        
        Object summary = result.get("summary");
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary instanceof String, "Summary should be a string");
        assertFalse(((String) summary).trim().isEmpty(), "Summary should not be empty");
        
        Object totalResults = result.get("totalResults");
        assertNotNull(totalResults, "Total results should not be null");
        assertTrue(((Number) totalResults).intValue() > 0, "Should have found some results");
        
        Object processingTime = result.get("processingTimeMs");
        assertNotNull(processingTime, "Processing time should be recorded");
        
        logger.info("Search and summarize successful:");
        logger.info("  - Query: {}", query);
        logger.info("  - Results found: {}", totalResults);
        logger.info("  - Processing time: {}ms", processingTime);
        logger.info("  - Summary: {}", summary);
    }
    
    @Test
    @Order(4)
    @DisplayName("Test custom summarization with specific parameters")
    void testCustomSummarization() throws Exception {
        logger.info("Testing custom summarization");
        
        // First get search results
        SearchRequest searchRequest = SearchRequest.builder()
            .query("programming python javascript")
            .indexName(TEST_INDEX)
            .maxResults(3)
            .minScore(0.01)
            .build();
        
        SearchResponse searchResponse = client.search(searchRequest);
        assertTrue(searchResponse.isSuccess(), "Search should be successful");
        assertFalse(searchResponse.getResults().isEmpty(), "Should have search results");
        
        // Now test custom summarization
        SummarizationRequest summaryRequest = new SummarizationRequest(
            "programming python javascript", 
            searchResponse.getResults()
        );
        summaryRequest.setMaxSummaryLength(200);
        summaryRequest.setCustomPrompt("Focus on beginner-friendly aspects and practical applications");
        summaryRequest.setIncludeSourceReferences(true);
        
        SummarizationResponse summaryResponse = client.summarize(summaryRequest);
        
        assertNotNull(summaryResponse, "Summary response should not be null");
        assertTrue(summaryResponse.isSuccess(), "Summarization should be successful");
        assertNotNull(summaryResponse.getSummary(), "Summary should not be null");
        assertFalse(summaryResponse.getSummary().trim().isEmpty(), "Summary should not be empty");
        assertEquals(searchResponse.getResults().size(), summaryResponse.getSourceCount(), 
            "Source count should match number of input results");
        assertNotNull(summaryResponse.getSourceReferences(), "Source references should be included");
        assertTrue(summaryResponse.getProcessingTimeMs() > 0, "Processing time should be positive");
        
        logger.info("Custom summarization successful:");
        logger.info("  - Processing time: {}ms", summaryResponse.getProcessingTimeMs());
        logger.info("  - Summary length: {} characters", summaryResponse.getSummary().length());
        logger.info("  - Source count: {}", summaryResponse.getSourceCount());
        logger.info("  - Summary: {}", summaryResponse.getSummary());
    }
    
    @Test
    @Order(5)
    @DisplayName("Test summarization with simple convenience method")
    void testSimpleSummarization() throws Exception {
        logger.info("Testing simple summarization convenience method");
        
        // Get search results first
        SearchResponse searchResponse = client.search("artificial intelligence", TEST_INDEX);
        assertTrue(searchResponse.isSuccess(), "Search should be successful");
        assertFalse(searchResponse.getResults().isEmpty(), "Should have search results");
        
        // Use convenience method
        SummarizationResponse summaryResponse = client.summarize(
            "artificial intelligence", 
            searchResponse.getResults()
        );
        
        assertNotNull(summaryResponse, "Summary response should not be null");
        assertTrue(summaryResponse.isSuccess(), "Summarization should be successful");
        assertNotNull(summaryResponse.getSummary(), "Summary should not be null");
        assertFalse(summaryResponse.getSummary().trim().isEmpty(), "Summary should not be empty");
        
        logger.info("Simple summarization successful:");
        logger.info("  - Summary: {}", summaryResponse.getSummary());
    }
    
    @Test
    @Order(6)
    @DisplayName("Test summarization performance")
    void testSummarizationPerformance() throws Exception {
        logger.info("Testing summarization performance");
        
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> result = client.searchAndSummarize(
            "programming artificial intelligence machine learning", 
            TEST_INDEX, 
            10,  // Get more results to test performance
            0.001  // Very low threshold to get more results
        );
        
        long clientTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(result, "Result should not be null");
        assertEquals(Boolean.TRUE, result.get("success"), "Operation should be successful");
        
        Object processingTime = result.get("processingTimeMs");
        assertNotNull(processingTime, "Processing time should be recorded");
        
        long serverTime = ((Number) processingTime).longValue();
        
        logger.info("Performance test results:");
        logger.info("  - Total client time: {}ms", clientTime);
        logger.info("  - Server processing time: {}ms", serverTime);
        logger.info("  - Results processed: {}", result.get("totalResults"));
        
        // Performance assertions (adjust thresholds as needed)
        assertTrue(serverTime < 60000, "Server processing should complete within 60 seconds");
        assertTrue(clientTime < 90000, "Total client time should be within 90 seconds");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test error handling with invalid input")
    void testErrorHandling() throws Exception {
        logger.info("Testing error handling with invalid input");
        
        // Test with empty search results
        SummarizationRequest invalidRequest = new SummarizationRequest("test query", java.util.Collections.emptyList());
        
        try {
            SummarizationResponse response = client.summarize(invalidRequest);
            
            // Should either throw an exception or return unsuccessful response
            if (response != null) {
                assertFalse(response.isSuccess(), "Should not succeed with empty search results");
                logger.info("Error handling working correctly: {}", response.getSummary());
            }
        } catch (Exception e) {
            logger.info("Error handling working correctly, exception thrown: {}", e.getMessage());
            // This is expected behavior
        }
    }
}
