# RAG Service with Spring Boot AI and OpenSearch

A production-ready Retrieval Augmented Generation (RAG) service built with Spring Boot, Spring AI, and OpenSearch. This service provides enterprise-grade vector-based document storage, retrieval, and search capabilities with comprehensive support for CSV document ingestion, local Llama model integration, and a fully-featured Java client library.

Designed for scalability and production deployment, the service supports local development, Docker containerization, and Kubernetes orchestration with complete observability and health monitoring.

## Architecture Overview

### System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client Apps   │    │   REST API       │    │  Spring Boot    │
│                 │◄──►│  Controllers     │◄──►│  Application    │
│ • Web UI        │    │ • RagController  │    │                 │
│ • CLI Tools     │    │ • IndexController│    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐              │
                       │   Spring AI     │◄─────────────┤
                       │                 │              │
                       │ • Ollama/Llama  │              │
                       │ • Embeddings    │              │
                       └─────────────────┘              │
                                                        │
                       ┌─────────────────┐              │
                       │   OpenSearch    │◄─────────────┘
                       │                 │
                       │ • Vector Store  │
                       │ • k-NN Search   │
                       │ • Text Search   │
                       └─────────────────┘
```

### Key Components

1. **REST API Layer**
   - `RagController`: Document ingestion, search, and retrieval
   - `IndexController`: Index management operations

2. **Service Layer**
   - `EmbeddingService`: Vector embedding generation using Spring AI
   - `VectorStoreService`: OpenSearch operations and vector storage
   - `CsvProcessingService`: CSV file parsing and document extraction

3. **Data Layer**
   - OpenSearch: Vector database with k-NN search capabilities
   - Document storage with metadata and embeddings

4. **AI Integration**
   - Ollama/Llama integration through Spring AI
   - Local model support for embeddings generation

## Features

- 📄 **Document Ingestion**: Single document and CSV bulk upload
- 🔍 **Vector Search**: Similarity search using embeddings
- 🔗 **Hybrid Search**: Combined vector and text search
- 📝 **AI Summarization**: LLM-powered summaries of search results
- 📊 **Index Management**: Create, delete, and check index existence
- 🤖 **AI Integration**: Local Llama model support via Ollama
- ⚙️ **Configurable Models**: Separate models for embeddings and summarization
- 📈 **Scalable**: Built on Spring Boot with production-ready patterns
- 🧪 **Well Tested**: Comprehensive unit and integration tests

### 🆕 Document Processing Features

- 📁 **Directory Scanning**: Recursive directory scanning with document text extraction
- 📑 **Multi-Format Support**: Extract text from PDF, DOCX, TXT, RTF, HTML, XML, and 10+ more formats
- 📊 **CSV Streaming**: Memory-efficient processing of large CSV files with batch indexing
- ⚡ **Asynchronous Processing**: Non-blocking operations with real-time progress tracking
- 📈 **Progress Monitoring**: Real-time status updates with processing rates and progress percentages
- 🛑 **Cancellation Support**: Cancel long-running operations gracefully
- 🔧 **Configurable Processing**: Customizable batch sizes, file limits, and processing parameters
- 📋 **Comprehensive Metadata**: Rich metadata extraction including file properties and content statistics

## Prerequisites

Before running the service, ensure you have the following installed:

### Required Software

1. **Java 21** - The service is built for Java 21 (OpenJDK)
2. **Maven 3.8+** - For building the application
3. **OpenSearch 2.11+** - Vector database running on port 30920
4. **Ollama** - For local Llama model integration

### Environment Setup

#### 1. Install OpenJDK 21
```bash
# macOS (Homebrew)
brew install openjdk@21

# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# CentOS/RHEL
sudo yum install java-21-openjdk-devel
```

#### 2. Install Maven
```bash
# macOS (Homebrew)
brew install maven

# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven
```

#### 3. Set up OpenSearch
```bash
# Using Docker
docker run -d \
  --name opensearch-node \
  -p 30920:9200 \
  -p 9600:9600 \
  -e "discovery.type=single-node" \
  -e "plugins.security.disabled=true" \
  opensearchproject/opensearch:2.11.1
```

#### 4. Set up Ollama with Llama2
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Pull and run Llama2 model
ollama pull llama2
ollama serve
```

## Configuration

### Application Properties

The service can be configured via `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama2
      embedding:
        model: llama2

opensearch:
  host: localhost
  port: 30920
  scheme: http

rag:
  default-index-name: documents
  embedding-dimension: 4096
  max-search-results: 10
```

### Environment Variables

You can override configuration using environment variables:

```bash
export OPENSEARCH_HOST=localhost
export OPENSEARCH_PORT=30920
export OLLAMA_BASE_URL=http://localhost:11434
export RAG_EMBEDDING_DIMENSION=4096
```

## Building and Running

### 1. Clone and Build

```bash
cd /Users/chris/code/warp_experiments/java_rag
mvn clean compile
```

### 2. Run Tests

```bash
mvn test
```

### 3. Package the Application

```bash
mvn clean package
```

### 4. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or using the packaged JAR
java -jar target/rag-service-0.0.1-SNAPSHOT.jar
```

The service will start on `http://localhost:8080`

## API Documentation

### Index Management

#### Create Index
```bash
POST /api/indexes/{indexName}
```

#### Check Index Exists
```bash
GET /api/indexes/{indexName}/exists
```

#### Delete Index
```bash
DELETE /api/indexes/{indexName}
```

### Document Operations

#### Ingest Single Document
```bash
POST /api/rag/documents
Content-Type: application/json

{
  "content": "Your document content here",
  "indexName": "documents",
  "source": "manual-upload",
  "metadata": {
    "category": "technology",
    "author": "John Doe"
  }
}
```

#### Ingest CSV Documents
```bash
POST /api/rag/documents/csv
Content-Type: application/json

{
  "csvContent": "title,content,category\n\"Doc1\",\"Content1\",\"Tech\"",
  "indexName": "documents",
  "contentColumnName": "content",
  "source": "csv-upload"
}
```

#### Get Document
```bash
GET /api/rag/documents/{indexName}/{documentId}
```

### Search Operations

#### Vector Similarity Search
```bash
POST /api/rag/search
Content-Type: application/json

{
  "query": "machine learning algorithms",
  "indexName": "documents",
  "size": 10,
  "minScore": 0.5
}
```

#### Hybrid Search (Vector + Text)
```bash
POST /api/rag/search/hybrid
Content-Type: application/json

{
  "query": "artificial intelligence",
  "indexName": "documents",
  "size": 10,
  "minScore": 0.3
}
```

#### Summarize Search Results
```bash
POST /api/rag/summarize
Content-Type: application/json

{
  "query": "machine learning algorithms",
  "searchResults": [
    {
      "document": {
        "id": "1",
        "content": "Document content here...",
        "metadata": {"title": "ML Guide"},
        "source": "tech-docs"
      },
      "score": 0.85
    }
  ],
  "maxSummaryLength": 200,
  "includeSourceReferences": true,
  "customPrompt": "Focus on practical applications"
}
```

#### Search and Summarize (Combined)
```bash
POST /api/rag/search-and-summarize
Content-Type: application/json

{
  "query": "artificial intelligence applications",
  "indexName": "documents",
  "size": 10,
  "minScore": 0.3
}
```

### Document Processing Operations

#### Directory Scanning

Scan directories recursively and extract text from supported document formats.

**Start Directory Scan:**
```bash
POST /api/v1/document-processing/directory-scan
Content-Type: application/json

{
  "directory_path": "/path/to/documents",
  "output_csv_path": "/path/to/output.csv",
  "supported_extensions": ["pdf", "docx", "txt"],
  "recursive": true,
  "max_files": 1000
}
```

**Get Scan Status:**
```bash
GET /api/v1/document-processing/directory-scan/{scanId}
```

**Response:**
```json
{
  "scan_id": "scan_1694123456789_abcd1234",
  "status": "COMPLETED",
  "files_processed": 150,
  "files_failed": 2,
  "total_files_found": 152,
  "csv_output_path": "/path/to/output.csv",
  "start_time": "2024-09-08T10:30:00Z",
  "end_time": "2024-09-08T10:35:42Z",
  "duration_ms": 342000,
  "processed_extensions": ["pdf", "docx", "txt"],
  "errors": []
}
```

**Cancel Directory Scan:**
```bash
DELETE /api/v1/document-processing/directory-scan/{scanId}
```

**List Active Scans:**
```bash
GET /api/v1/document-processing/directory-scan
```

#### CSV Streaming

Stream and index large CSV files efficiently with batch processing.

**Start CSV Streaming:**
```bash
POST /api/v1/document-processing/csv-streaming
Content-Type: application/json

{
  "csv_file_path": "/path/to/large-file.csv",
  "batch_size": 200,
  "text_column": "content",
  "metadata_columns": ["title", "category", "author"],
  "skip_header": true,
  "delimiter": ",",
  "index_name": "documents"
}
```

**Get Stream Status:**
```bash
GET /api/v1/document-processing/csv-streaming/{streamId}
```

**Response:**
```json
{
  "stream_id": "stream_1694123456789_xyz9876",
  "status": "PROCESSING",
  "records_processed": 15420,
  "records_indexed": 14647,
  "records_failed": 773,
  "total_records": 50000,
  "batch_count": 77,
  "current_batch": 78,
  "progress_percentage": 30.84,
  "start_time": "2024-09-08T11:00:00Z",
  "duration_ms": 185000,
  "processing_rate_per_second": 83.35,
  "index_name": "documents",
  "errors": [],
  "warnings": []
}
```

**Cancel CSV Streaming:**
```bash
DELETE /api/v1/document-processing/csv-streaming/{streamId}
```

**List Active Streams:**
```bash
GET /api/v1/document-processing/csv-streaming
```

#### Utility Operations

**Get Supported Document Formats:**
```bash
GET /api/v1/document-processing/supported-formats
```

**Response:**
```json
{
  "supported_extensions": [
    "pdf", "txt", "docx", "doc", "rtf", "html", "htm", "xml",
    "odt", "ods", "odp", "pptx", "ppt", "xlsx", "xls", "csv"
  ],
  "total_count": 16,
  "description": "Supported document formats for text extraction"
}
```

**Estimate CSV Record Count:**
```bash
POST /api/v1/document-processing/csv-estimate
Content-Type: application/json

{
  "csv_file_path": "/path/to/file.csv",
  "skip_header": true
}
```

**Response:**
```json
{
  "csv_file_path": "/path/to/file.csv",
  "estimated_record_count": 45239,
  "skip_header": true,
  "status": "success"
}
```

**Health Check:**
```bash
GET /api/v1/document-processing/health
```

**Response:**
```json
{
  "status": "healthy",
  "services": {
    "directory_scan": "available",
    "csv_streaming": "available",
    "document_extraction": "available"
  },
  "timestamp": 1694123456789
}
```

## Quick Start Guide

### Option 1: Local Development (Traditional)

```bash
# 1. Start dependencies
docker run -d --name opensearch-dev -p 30920:9200 -e "discovery.type=single-node" -e "plugins.security.disabled=true" opensearchproject/opensearch:2.11.1
ollama pull llama2 && ollama serve

# 2. Build and run the service
mvn clean package
java -jar target/rag-service-0.0.1-SNAPSHOT.jar
```

### Option 2: Docker (Containerized)

```bash
# 1. Start dependencies
docker run -d --name opensearch-dev -p 30920:9200 -e "discovery.type=single-node" -e "plugins.security.disabled=true" opensearchproject/opensearch:2.11.1
docker run -d --name ollama-dev -p 11434:11434 ollama/ollama:latest
docker exec -it ollama-dev ollama pull llama2

# 2. Build and run service
./scripts/docker_build.sh
./scripts/docker_run.sh
```

### Option 3: Kubernetes (Production-Ready)

```bash
# Deploy complete stack
./scripts/k8s_deploy_all.sh

# Access via port-forward
kubectl port-forward service/java-rag-service 8080:80
```

## Example Usage

### Basic API Testing

#### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

#### 2. Create an Index
```bash
curl -X POST http://localhost:8080/api/indexes/tech-docs
```

#### 3. Upload Single Document
```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring Boot is a powerful framework for building Java applications with minimal configuration.",
    "indexName": "tech-docs",
    "source": "manual-test",
    "metadata": {
      "category": "Java Framework",
      "difficulty": "beginner"
    }
  }'
```

#### 4. Upload CSV Documents
```bash
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d '{
    "csvContent": "title,content,category\n\"Sample Doc 1\",\"This is the first sample document for testing the RAG service.\",\"Technology\"\n\"Sample Doc 2\",\"This is the second sample document about artificial intelligence.\",\"AI\"",
    "indexName": "tech-docs",
    "contentColumnName": "content",
    "source": "curl-test"
  }'
```

#### 5. Search Documents
```bash
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning",
    "indexName": "tech-docs",
    "size": 5,
    "minScore": 0.3
  }'
```

#### 6. Hybrid Search (Vector + Text)
```bash
curl -X POST http://localhost:8080/api/rag/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "artificial intelligence",
    "indexName": "tech-docs",
    "size": 10,
    "minScore": 0.3
  }'
```

#### 7. Search and Summarize (Combined)
```bash
curl -X POST http://localhost:8080/api/rag/search-and-summarize \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning algorithms",
    "indexName": "tech-docs",
    "size": 5,
    "minScore": 0.3
  }'
```

**Response includes both search results and AI-generated summary:**
```json
{
  "success": true,
  "query": "machine learning algorithms",
  "searchResults": [...],
  "totalResults": 3,
  "summary": "Machine learning algorithms are computational methods that enable computers to learn patterns from data. Popular algorithms include neural networks for deep learning, decision trees for classification, and support vector machines for both classification and regression tasks.",
  "sourceReferences": ["tech-docs", "ml-papers"],
  "model": "llama2",
  "processingTimeMs": 2340
}
```

### Working with Files

#### Upload from JSON File
```bash
# Create request file
cat > request.json << 'EOF'
{
  "csvContent": "title,content,category,author\n\"Introduction to Machine Learning\",\"Machine Learning is a subset of artificial intelligence that enables computers to learn and make decisions without being explicitly programmed.\",\"Technology\",\"Alice Johnson\"",
  "indexName": "tech-docs",
  "contentColumnName": "content",
  "source": "sample-tech-articles"
}
EOF

# Use it
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d @request.json
```

### Document Processing Examples

#### Directory Scanning Example
```bash
# 1. Test document processing health
curl http://localhost:8080/api/v1/document-processing/health

# 2. Get supported formats
curl http://localhost:8080/api/v1/document-processing/supported-formats

# 3. Start a directory scan
curl -X POST http://localhost:8080/api/v1/document-processing/directory-scan \
  -H "Content-Type: application/json" \
  -d '{
    "directory_path": "/Users/chris/Documents",
    "output_csv_path": "/tmp/extracted_documents.csv",
    "recursive": true,
    "max_files": 50,
    "supported_extensions": ["pdf", "txt", "docx"]
  }'

# Response: {"scan_id": "scan_1694123456789_abcd1234", "status": "STARTED", ...}

# 4. Check scan progress
curl http://localhost:8080/api/v1/document-processing/directory-scan/scan_1694123456789_abcd1234

# 5. List all active scans
curl http://localhost:8080/api/v1/document-processing/directory-scan
```

#### CSV Streaming Example
```bash
# 1. Estimate record count first
curl -X POST http://localhost:8080/api/v1/document-processing/csv-estimate \
  -H "Content-Type: application/json" \
  -d '{
    "csv_file_path": "/path/to/large-dataset.csv",
    "skip_header": true
  }'

# 2. Start CSV streaming
curl -X POST http://localhost:8080/api/v1/document-processing/csv-streaming \
  -H "Content-Type: application/json" \
  -d '{
    "csv_file_path": "/path/to/large-dataset.csv",
    "batch_size": 100,
    "text_column": "content",
    "metadata_columns": ["title", "category", "author"],
    "skip_header": true,
    "index_name": "documents"
  }'

# Response: {"stream_id": "stream_1694123456789_xyz9876", "status": "STARTED", ...}

# 3. Monitor streaming progress
curl http://localhost:8080/api/v1/document-processing/csv-streaming/stream_1694123456789_xyz9876

# 4. Cancel if needed
curl -X DELETE http://localhost:8080/api/v1/document-processing/csv-streaming/stream_1694123456789_xyz9876
```

#### Complete Workflow Example
```bash
# Complete workflow: Extract documents, then stream to index

# Step 1: Scan directory and extract to CSV
SCAN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/document-processing/directory-scan \
  -H "Content-Type: application/json" \
  -d '{
    "directory_path": "/Users/chris/research-papers",
    "output_csv_path": "/tmp/research_papers.csv",
    "recursive": true,
    "supported_extensions": ["pdf", "docx", "txt"]
  }')

SCAN_ID=$(echo $SCAN_RESPONSE | jq -r '.scan_id')
echo "Started directory scan: $SCAN_ID"

# Step 2: Wait for completion (poll status)
while true; do
  STATUS=$(curl -s "http://localhost:8080/api/v1/document-processing/directory-scan/$SCAN_ID" | jq -r '.status')
  echo "Scan status: $STATUS"
  if [ "$STATUS" = "COMPLETED" ]; then
    break
  fi
  sleep 5
done

# Step 3: Stream the CSV to search index
STREAM_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/document-processing/csv-streaming \
  -H "Content-Type: application/json" \
  -d '{
    "csv_file_path": "/tmp/research_papers.csv",
    "batch_size": 50,
    "text_column": "text",
    "metadata_columns": ["file_name", "file_path", "content_type"],
    "skip_header": true,
    "index_name": "research-papers"
  }')

STREAM_ID=$(echo $STREAM_RESPONSE | jq -r '.stream_id')
echo "Started CSV streaming: $STREAM_ID"

# Step 4: Monitor streaming progress
while true; do
  PROGRESS=$(curl -s "http://localhost:8080/api/v1/document-processing/csv-streaming/$STREAM_ID")
  STATUS=$(echo $PROGRESS | jq -r '.status')
  PERCENTAGE=$(echo $PROGRESS | jq -r '.progress_percentage')
  echo "Streaming status: $STATUS, Progress: $PERCENTAGE%"
  if [ "$STATUS" = "COMPLETED" ]; then
    break
  fi
  sleep 10
done

echo "Document processing complete! Ready to search."
```

For complete cURL examples and troubleshooting, see [CURL_EXAMPLES.md](CURL_EXAMPLES.md).

## Sample Data

The repository includes three example CSV files in the `examples/` directory:

1. **`tech_articles.csv`** - Technology articles and tutorials
2. **`faq_documents.csv`** - Frequently asked questions
3. **`research_papers.csv`** - Academic research paper abstracts

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/ragservice/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── model/          # Domain models
│   │   └── service/        # Business logic services
│   └── resources/
│       └── application.yml  # Configuration
├── test/
│   └── java/               # Unit and integration tests
└── examples/               # Sample CSV files
```

### Adding New Features

1. **New Document Types**: Extend `CsvProcessingService` to support additional formats
2. **Custom Embeddings**: Implement different embedding models in `EmbeddingService`
3. **Advanced Search**: Add new search strategies in `VectorStoreService`
4. **New APIs**: Create additional controllers for extended functionality

## Monitoring and Health Checks

The application includes Spring Boot Actuator endpoints:

- **Health Check**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

## Troubleshooting

### Common Issues

1. **OpenSearch Connection Failed**
   - Verify OpenSearch is running on port 30920
   - Check network connectivity: `curl http://localhost:30920`

2. **Ollama Connection Failed**
   - Ensure Ollama is running: `ollama list`
   - Verify Llama2 model is available: `ollama show llama2`

3. **Java Version Issues**
   - Verify Java 21: `java -version`
   - Check JAVA_HOME environment variable

4. **Embedding Generation Slow**
   - Consider using a GPU-optimized Ollama setup
   - Adjust batch sizes in embedding service
   - Monitor system resources

### Logging

Set logging levels in `application.yml`:

```yaml
logging:
  level:
    com.example.ragservice: DEBUG
    org.opensearch: INFO
    org.springframework.ai: DEBUG
```

## Java Client Library

The service includes a comprehensive Java client library for easy integration with your applications. The client provides type-safe operations for all service functionality.

### Quick Start with Client Library

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

### Client Configuration

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

### Core Client Operations

#### Document Upload
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

#### Search Operations
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

#### Summarization Operations
```java
// Search and summarize in one call
Map<String, Object> result = client.searchAndSummarize("machine learning", "tech-docs");
System.out.println("Summary: " + result.get("summary"));
System.out.println("Sources: " + result.get("sourceReferences"));

// Summarize existing search results
SearchResponse searchResults = client.search("AI algorithms", "tech-docs");
SummarizationResponse summary = client.summarize("AI algorithms", searchResults.getResults());
System.out.println("Generated summary: " + summary.getSummary());

// Advanced summarization with custom options
SummarizationRequest summaryRequest = new SummarizationRequest(
    "deep learning applications", 
    searchResults.getResults()
);
summaryRequest.setMaxSummaryLength(300);
summaryRequest.setCustomPrompt("Focus on real-world applications");
summaryRequest.setIncludeSourceReferences(true);

SummarizationResponse detailedSummary = client.summarize(summaryRequest);
System.out.println("Custom summary: " + detailedSummary.getSummary());
System.out.println("Processing time: " + detailedSummary.getProcessingTimeMs() + "ms");
```

### Client Library Features

- **Type Safety**: Full type safety with proper DTOs and error handling
- **Auto-Closeable**: Implements AutoCloseable for proper resource management
- **Configurable**: Flexible timeout, retry, and connection settings
- **Thread Safe**: Can be shared across multiple threads
- **Comprehensive Testing**: Extensive unit and integration tests

For complete client library documentation, see [README-RAG-CLIENT.md](README-RAG-CLIENT.md).

## Docker Deployment

The service includes complete Docker support with multi-stage builds and production-ready configurations.

### Build and Run with Docker

```bash
# Build the Docker image
./scripts/docker_build.sh

# Run the container (expects external dependencies)
./scripts/docker_run.sh
```

### Docker Configuration

The `Dockerfile` uses a multi-stage build:
- **Build Stage**: Uses Maven with Amazon Corretto 21 to compile the application
- **Runtime Stage**: Uses lightweight Amazon Corretto 21 Alpine image
- **Security**: Runs as non-root user with proper permissions
- **Health Checks**: Built-in health check endpoint monitoring

### Docker Environment Variables

```bash
# Override default settings
HOST_PORT_APP=8080 ./scripts/docker_run.sh
OPENSEARCH_HOST=my-opensearch.com ./scripts/docker_run.sh
OLLAMA_BASE_URL=http://my-ollama:11434 ./scripts/docker_run.sh
```

### Running Dependencies with Docker

```bash
# Start OpenSearch
docker run -d --name opensearch-dev \
  -p 30920:9200 \
  -e "discovery.type=single-node" \
  -e "plugins.security.disabled=true" \
  opensearchproject/opensearch:2.11.1

# Start Ollama
docker run -d --name ollama-dev \
  -p 11434:11434 \
  ollama/ollama:latest
  
# Pull model
docker exec -it ollama-dev ollama pull llama2
```

## Kubernetes Deployment

The service includes production-ready Kubernetes manifests with complete observability, health checks, and resource management.

### Quick Kubernetes Deployment

```bash
# Deploy complete stack (OpenSearch + Ollama + App)
./scripts/k8s_deploy_all.sh

# Or deploy just the app (if you have external dependencies)
./scripts/k8s_deploy_app.sh

# Clean up
./scripts/k8s_delete_all.sh
```

### Kubernetes Architecture

The Kubernetes setup includes:

#### Application Resources
- **Deployment**: 2 replicas with rolling updates
- **Service**: LoadBalancer with ports 80 and 8080
- **Health Checks**: Liveness, readiness, and startup probes
- **Resource Limits**: CPU and memory constraints

#### Optional Dependencies
- **OpenSearch**: Single-node cluster with persistent storage
- **Ollama**: GPU-ready deployment with model initialization
- **Services**: ClusterIP services for internal communication

### Kubernetes Configuration

```yaml
# App deployment with environment variables
env:
- name: SPRING_PROFILES_ACTIVE
  value: "k8s"
- name: OPENSEARCH_HOST
  value: "opensearch-service"
- name: OLLAMA_BASE_URL
  value: "http://ollama-service:11434"
- name: JAVA_OPTS
  value: "-Xmx1g -Xms512m"
```

### Resource Requirements

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|------------|-----------|-----------------|---------------|
| Java RAG Service | 250m | 1000m | 768Mi | 1536Mi |
| OpenSearch | 500m | 1000m | 1.5Gi | 2Gi |
| Ollama | 500m | 2000m | 2Gi | 4Gi |

### Accessing the Service

```bash
# Check service status
kubectl get pods
kubectl get services

# Port forward for local access
kubectl port-forward service/java-rag-service 8080:80

# Check logs
kubectl logs -l app=java-rag-service -f
```

### Health Monitoring

Kubernetes deployment includes comprehensive health monitoring:

- **Startup Probes**: Wait for application to start (up to 2 minutes)
- **Readiness Probes**: Check if ready to receive traffic
- **Liveness Probes**: Restart unhealthy pods
- **Actuator Endpoints**: Spring Boot health and metrics

## Performance Considerations

1. **Embedding Batch Size**: Process documents in batches for better performance
2. **OpenSearch Tuning**: Configure appropriate heap size and JVM settings
3. **Connection Pooling**: Adjust OpenSearch client connection settings
4. **Caching**: Consider adding caching for frequently accessed documents
5. **Resource Scaling**: Use Kubernetes HPA for automatic scaling
6. **Load Balancing**: Multiple service replicas for high availability

## Security Considerations

1. **Authentication**: Add Spring Security for API authentication
2. **Input Validation**: All inputs are validated using Bean Validation
3. **Data Encryption**: Enable TLS for OpenSearch connections in production
4. **API Rate Limiting**: Implement rate limiting for public endpoints

## Production Deployment

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/rag-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment-Specific Configurations

Use Spring profiles for different environments:

- `application-dev.yml`
- `application-staging.yml`
- `application-prod.yml`

### Health Monitoring

Integrate with monitoring solutions:
- Prometheus metrics via Micrometer
- Application logs via structured logging
- Custom health indicators for dependencies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add comprehensive tests
4. Update documentation
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review application logs
3. Create an issue in the repository
4. Consult Spring Boot and Spring AI documentation
