# API Usage Examples

This document provides practical examples of how to use the RAG Service APIs.

## Prerequisites

Before running these examples, ensure:
1. The RAG service is running on `http://localhost:8080`
2. OpenSearch is running on port 30920
3. Ollama with Llama2 is available

## Index Management Examples

### Create a New Index

```bash
curl -X POST http://localhost:8080/api/indexes/knowledge-base \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "indexName": "knowledge-base",
  "message": "Index created successfully"
}
```

### Check if Index Exists

```bash
curl -X GET http://localhost:8080/api/indexes/knowledge-base/exists
```

**Expected Response:**
```json
{
  "success": true,
  "indexName": "knowledge-base",
  "exists": true
}
```

### Delete an Index

```bash
curl -X DELETE http://localhost:8080/api/indexes/knowledge-base
```

**Expected Response:**
```json
{
  "success": true,
  "indexName": "knowledge-base",
  "message": "Index deleted successfully"
}
```

## Document Ingestion Examples

### Ingest a Single Document

```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring Boot is a Java framework that simplifies the development of microservices and web applications. It provides auto-configuration, embedded servers, and production-ready features out of the box.",
    "indexName": "tech-docs",
    "source": "manual-entry",
    "metadata": {
      "category": "Java Framework",
      "author": "Tech Writer",
      "difficulty": "intermediate",
      "tags": ["spring-boot", "java", "framework"]
    }
  }'
```

```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
  "csvContent": "title,content,category\n\"Doc 1\",\"Content here\",\"Tech\"",
  "indexName": "my-index",
  "contentColumnName": "content",
  "source": "tech_articles.csv"
}
  ```

**Expected Response:**
```json
{
  "success": true,
  "documentId": "abc123-def456-ghi789",
  "indexName": "tech-docs"
}
```

### Ingest Documents from CSV

```bash
# First, let's create a simple CSV content
CSV_CONTENT='title,content,category,author
"Docker Basics","Docker is a containerization platform that allows developers to package applications and their dependencies into lightweight, portable containers.","DevOps","John Smith"
"Kubernetes Overview","Kubernetes is an open-source container orchestration platform that automates deployment, scaling, and management of containerized applications.","DevOps","Jane Doe"'

curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d "{
    \"csvContent\": \"$CSV_CONTENT\",
    \"indexName\": \"tech-docs\",
    \"contentColumnName\": \"content\",
    \"source\": \"csv-import\"
  }"
```

**Expected Response:**
```json
{
  "success": true,
  "documentsIngested": 2,
  "indexName": "tech-docs",
  "headers": ["title", "content", "category", "author"]
}
```

### Ingest from Example CSV Files

```bash
# Upload technology articles
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d "{
    \"csvContent\": \"$(cat examples/tech_articles.csv | jq -R -s .)\",
    \"indexName\": \"tech-articles\",
    \"contentColumnName\": \"content\",
    \"source\": \"tech-articles-csv\"
  }"

# Upload FAQ documents  
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d "{
    \"csvContent\": \"$(cat examples/faq_documents.csv | jq -R -s .)\",
    \"indexName\": \"faq-docs\",
    \"contentColumnName\": \"content\",
    \"source\": \"faq-csv\"
  }"

# Upload research papers
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d "{
    \"csvContent\": \"$(cat examples/research_papers.csv | jq -R -s .)\",
    \"indexName\": \"research-papers\",
    \"contentColumnName\": \"content\",
    \"source\": \"research-csv\"
  }"
```

## Search Examples

### Vector Similarity Search

```bash
# Search for machine learning content
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning algorithms and neural networks",
    "indexName": "tech-articles",
    "size": 5,
    "minScore": 0.3
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "query": "machine learning algorithms and neural networks",
  "results": [
    {
      "document": {
        "id": "doc-123",
        "content": "Machine Learning is a subset of artificial intelligence...",
        "metadata": {
          "title": "Introduction to Machine Learning",
          "category": "Technology",
          "author": "Alice Johnson"
        },
        "timestamp": "2024-01-15T10:30:00Z",
        "source": "tech-articles-csv"
      },
      "score": 0.85
    }
  ],
  "totalResults": 1
}
```

### Hybrid Search (Vector + Text)

```bash
# Hybrid search combining vector similarity and text matching
curl -X POST http://localhost:8080/api/rag/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "cybersecurity threats and protection",
    "indexName": "tech-articles",
    "size": 3,
    "minScore": 0.2
  }'
```

### Search FAQ Documents

```bash
# Search for password reset information
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "how to reset password forgot login",
    "indexName": "faq-docs",
    "size": 3,
    "minScore": 0.4
  }'
```

### Search Research Papers

```bash
# Search for quantum computing research
curl -X POST http://localhost:8080/api/rag/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "quantum computing machine learning algorithms",
    "indexName": "research-papers",
    "size": 5,
    "minScore": 0.3
  }'
```

## Document Retrieval Examples

### Get Specific Document

```bash
# Replace with actual document ID from search results
curl -X GET http://localhost:8080/api/rag/documents/tech-articles/abc123-def456-ghi789
```

**Expected Response:**
```json
{
  "success": true,
  "document": {
    "id": "abc123-def456-ghi789",
    "content": "Machine Learning is a subset of artificial intelligence...",
    "metadata": {
      "title": "Introduction to Machine Learning",
      "category": "Technology",
      "author": "Alice Johnson",
      "csv_record_number": 1
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "source": "tech-articles-csv",
    "embedding": [0.1, 0.2, 0.3, ...]
  }
}
```

## Advanced Usage Examples

### Multi-Index Search

```bash
# Search across multiple indexes (requires multiple API calls)
for index in "tech-articles" "faq-docs" "research-papers"; do
  echo "Searching in $index:"
  curl -s -X POST http://localhost:8080/api/rag/search \
    -H "Content-Type: application/json" \
    -d "{
      \"query\": \"artificial intelligence\",
      \"indexName\": \"$index\",
      \"size\": 2,
      \"minScore\": 0.3
    }" | jq '.results[].document.metadata.title // .results[].document.content[:100]'
  echo "---"
done
```

### Search with Different Scoring Thresholds

```bash
# High relevance search (strict)
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "blockchain technology applications",
    "indexName": "tech-articles",
    "size": 10,
    "minScore": 0.7
  }'

# Broad search (permissive)
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "blockchain technology applications",
    "indexName": "tech-articles",
    "size": 10,
    "minScore": 0.1
  }'
```

### Batch Document Ingestion Script

```bash
#!/bin/bash
# batch_upload.sh - Upload multiple CSV files

declare -A csv_files=(
  ["tech-articles"]="examples/tech_articles.csv"
  ["faq-docs"]="examples/faq_documents.csv"
  ["research-papers"]="examples/research_papers.csv"
)

for index_name in "${!csv_files[@]}"; do
  csv_file="${csv_files[$index_name]}"
  
  echo "Uploading $csv_file to index $index_name..."
  
  # Create index first
  curl -s -X POST "http://localhost:8080/api/indexes/$index_name" > /dev/null
  
  # Upload CSV
  response=$(curl -s -X POST http://localhost:8080/api/rag/documents/csv \
    -H "Content-Type: application/json" \
    -d "{
      \"csvContent\": $(cat "$csv_file" | jq -R -s .),
      \"indexName\": \"$index_name\",
      \"contentColumnName\": \"content\",
      \"source\": \"$csv_file\"
    }")
  
  success=$(echo $response | jq -r '.success')
  if [ "$success" = "true" ]; then
    docs_count=$(echo $response | jq -r '.documentsIngested')
    echo "✅ Successfully uploaded $docs_count documents to $index_name"
  else
    error=$(echo $response | jq -r '.error')
    echo "❌ Failed to upload to $index_name: $error"
  fi
  echo "---"
done
```

## Health Check and Monitoring

### Application Health Check

```bash
curl -X GET http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1000000000,
        "free": 500000000,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Application Metrics

```bash
curl -X GET http://localhost:8080/actuator/metrics
```

## Error Handling Examples

### Invalid Request Examples

```bash
# Missing required fields
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "",
    "indexName": "test-docs"
  }'
```

**Expected Error Response:**
```json
{
  "success": false,
  "error": "Validation failed: Content cannot be blank"
}
```

```bash
# Invalid CSV format
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d '{
    "csvContent": "invalid csv content without headers",
    "indexName": "test-docs"
  }'
```

**Expected Error Response:**
```json
{
  "success": false,
  "error": "Invalid CSV format"
}
```

## Performance Testing Examples

### Load Testing Search Operations

```bash
#!/bin/bash
# Simple load test for search operations

queries=("machine learning" "artificial intelligence" "cybersecurity" "cloud computing" "blockchain")

for i in {1..50}; do
  query=${queries[$RANDOM % ${#queries[@]}]}
  
  start_time=$(date +%s%N)
  curl -s -X POST http://localhost:8080/api/rag/search \
    -H "Content-Type: application/json" \
    -d "{
      \"query\": \"$query\",
      \"indexName\": \"tech-articles\",
      \"size\": 5,
      \"minScore\": 0.3
    }" > /dev/null
  end_time=$(date +%s%N)
  
  duration=$(( (end_time - start_time) / 1000000 ))
  echo "Request $i: Query '$query' completed in ${duration}ms"
done
```

This document provides comprehensive examples for using the RAG Service APIs. Adjust the endpoints, index names, and parameters according to your specific use case.
