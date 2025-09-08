# RAG Java Client Library

A comprehensive Java client library for interacting with the RAG (Retrieval-Augmented Generation) service. This client provides a simple and type-safe way to perform vector searches, upload documents, and manage your RAG vector store.

## Features

- **Vector Search**: Perform semantic similarity searches using vector embeddings
- **Document Upload**: Upload individual documents or CSV files for indexing
- **Flexible Configuration**: Configurable timeouts, retries, and connection settings
- **Type Safety**: Full type safety with proper DTOs and error handling
- **Auto-Closeable**: Implements AutoCloseable for proper resource management
- **Comprehensive Testing**: Extensive unit and integration tests

## Quick Start

### Basic Usage

```java
// Create a client with default settings
try (RagClient client = RagClientImpl.create("http://localhost:8080")) {
    // Check if service is healthy
    if (client.isHealthy()) {
        System.out.println("RAG service is ready!");
        
        // Perform a simple search
        SearchResponse results = client.search("machine learning", "my-index");
        System.out.println("Found " + results.getTotalResults() + " results");
        
        // Print results
        results.getResults().forEach(result -> {
            System.out.println("Score: " + result.getScore());
            System.out.println("Content: " + result.getDocument().getContent());
        });
    }
}
```

### Advanced Configuration

```java
// Create client with custom configuration
RagClientConfig config = RagClientConfig.builder()
    .baseUrl("https://your-rag-service.com")
    .connectTimeout(Duration.ofSeconds(30))
    .readTimeout(Duration.ofSeconds(60))
    .maxRetries(3)
    .retryDelay(Duration.ofSeconds(1))
    .build();

try (RagClient client = new RagClientImpl(config)) {
    // Your RAG operations here
}
```

## Core Operations

### 1. Search Operations

```java
// Simple search
SearchResponse response = client.search("artificial intelligence", "tech-articles");

// Advanced search with parameters
SearchResponse response = client.search("AI ethics", "tech-articles", 10, 0.5);

// Search with request object
SearchRequest request = SearchRequest.builder()
    .query("neural networks")
    .indexName("research-papers")
    .maxResults(5)
    .minScore(0.3)
    .vectorSearch() // or .hybridSearch()
    .build();
SearchResponse response = client.search(request);
```

### 2. Document Upload

```java
// Upload CSV content
String csvData = "title,content,author\n" +
                 "AI Overview,Introduction to artificial intelligence,John Doe";

CsvUploadRequest csvRequest = CsvUploadRequest.builder()
    .csvContent(csvData)
    .indexName("articles")
    .contentColumnName("content")
    .source("my-application")
    .build();

CsvUploadResponse csvResponse = client.uploadCsv(csvRequest);
System.out.println("Indexed " + csvResponse.getDocumentsIndexed() + " documents");
```

```java
// Upload CSV file
File csvFile = new File("articles.csv");
CsvUploadResponse response = client.uploadCsvFile(csvFile, "articles", "content");
```

```java
// Upload individual document
Document document = new Document();
document.setId("doc-1");
document.setContent("This is my document content");
document.setMetadata(Map.of(
    "title", "My Document",
    "author", "Jane Smith"
));

String documentId = client.uploadDocument(document, "my-index");
```

### 3. Health and Service Info

```java
// Check service health
boolean isHealthy = client.isHealthy();

// Get service information
String serviceInfo = client.getServiceInfo();
```

## Error Handling

The client uses `RagClientException` for all operation failures:

```java
try {
    SearchResponse results = client.search("query", "index");
    // Process results
} catch (RagClientException e) {
    System.err.println("Search failed: " + e.getMessage());
    
    if (e.hasStatusCode()) {
        System.err.println("HTTP Status: " + e.getStatusCode());
        System.err.println("Error Body: " + e.getErrorBody());
    }
}
```

## Response Objects

### SearchResponse
- `query`: The original search query
- `results`: List of SearchResult objects
- `totalResults`: Total number of matching documents
- `success`: Whether the operation was successful

### SearchResult
- `document`: The matched Document object with full content and metadata
- `score`: Similarity score (0.0 to 1.0)

### Document
- `id`: Unique document identifier
- `content`: Full document content
- `metadata`: Key-value metadata pairs
- `embedding`: Vector embedding (if included)
- `timestamp`: Document creation timestamp
- `source`: Source identifier

## Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `baseUrl` | `http://localhost:8080` | RAG service base URL |
| `connectTimeout` | 30 seconds | Connection timeout |
| `readTimeout` | 60 seconds | Read timeout |
| `maxRetries` | 3 | Maximum retry attempts |
| `retryDelay` | 1 second | Delay between retries |

## Testing

### Unit Tests
```bash
mvn test -Dtest=RagClientTest
```

### Integration Tests
```bash
# Requires running RAG service
mvn test -Dintegration.test.enabled=true -Drag.service.url=http://localhost:8080
```

## Factory Methods

```java
// Default configuration
RagClient client = RagClientImpl.create();

// With base URL
RagClient client = RagClientImpl.create("http://my-rag-service:8080");

// With custom configuration
RagClientConfig config = RagClientConfig.builder()
    .baseUrl("http://my-rag-service:8080")
    .build();
RagClient client = RagClientImpl.create(config);
```

## Dependencies

The client library requires:
- Java 21+
- Jackson for JSON processing
- Apache HttpClient 5 for HTTP operations
- SLF4J for logging (optional)

## Example Application

See `RagClientExample.java` for a complete example demonstrating all client features.

## Thread Safety

The `RagClient` is thread-safe and can be shared across multiple threads. However, each client maintains its own HTTP connection pool, so it's recommended to reuse client instances when possible.

## Resource Management

Always use try-with-resources or manually call `close()` to properly release HTTP connections:

```java
try (RagClient client = RagClientImpl.create()) {
    // Your operations
} // Client is automatically closed
```

## Performance Tips

1. **Reuse client instances** - Creating a new client is expensive
2. **Configure appropriate timeouts** - Based on your network conditions
3. **Use batch operations** - For multiple document uploads
4. **Set reasonable result limits** - To avoid large response payloads
5. **Monitor connection pool** - Default pool size should handle most use cases
