# RAG Service with Spring Boot AI and OpenSearch

A production-ready Retrieval Augmented Generation (RAG) service built with Spring Boot, Spring AI, and OpenSearch. This service provides vector-based document storage, retrieval, and search capabilities with support for CSV document ingestion and local Llama model integration.

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
- 📊 **Index Management**: Create, delete, and check index existence
- 🤖 **AI Integration**: Local Llama model support via Ollama
- 📈 **Scalable**: Built on Spring Boot with production-ready patterns
- 🧪 **Well Tested**: Comprehensive unit and integration tests

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

## Example Usage

### 1. Create an Index
```bash
curl -X POST http://localhost:8080/api/indexes/tech-docs
```

### 2. Upload Sample CSV
```bash
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d '{
    "csvContent": "'"$(cat examples/tech_articles.csv | sed 's/"/\\"/g' | tr '\n' '\\n')"'",
    "indexName": "tech-docs",
    "contentColumnName": "content",
    "source": "tech-articles"
  }'
```

### 3. Search Documents
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

## Performance Considerations

1. **Embedding Batch Size**: Process documents in batches for better performance
2. **OpenSearch Tuning**: Configure appropriate heap size and JVM settings
3. **Connection Pooling**: Adjust OpenSearch client connection settings
4. **Caching**: Consider adding caching for frequently accessed documents

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
