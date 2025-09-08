# Working cURL Examples for RAG Service

## The Issue You Encountered

Your original command was missing the required request body:
```bash
curl -X POST http://localhost:8080/api/rag/documents/csv
```

**Problem**: This sends an empty POST request, but the API expects a JSON payload with the CSV content.

## Correct Usage Examples

### 1. CSV Document Upload (Working Example)

```bash
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d '{
    "csvContent": "title,content,category\n\"Sample Doc 1\",\"This is the first sample document for testing the RAG service.\",\"Technology\"\n\"Sample Doc 2\",\"This is the second sample document about artificial intelligence.\",\"AI\"",
    "indexName": "test-documents",
    "contentColumnName": "content",
    "source": "curl-test"
  }'
```

**Expected Response:**
```json
{
  "headers": ["title", "content", "category"],
  "success": true,
  "indexName": "test-documents",
  "documentsIngested": 2
}
```

### 2. Using JSON File (Recommended for Complex Data)

Create a JSON file first:
```bash
cat > request.json << 'EOF'
{
  "csvContent": "title,content,category,author\n\"Introduction to Machine Learning\",\"Machine Learning is a subset of artificial intelligence that enables computers to learn and make decisions without being explicitly programmed.\",\"Technology\",\"Alice Johnson\"\n\"Understanding Microservices\",\"Microservices architecture is a software development approach where applications are built as a collection of loosely coupled services.\",\"Technology\",\"Bob Smith\"",
  "indexName": "tech-docs",
  "contentColumnName": "content",
  "source": "sample-tech-articles"
}
EOF
```

Then use it:
```bash
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d @request.json
```

### 3. Single Document Upload

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

### 4. Search Documents (Requires OpenSearch Running)

```bash
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning artificial intelligence",
    "indexName": "tech-docs",
    "size": 5,
    "minScore": 0.1
  }'
```

### 5. Index Management

**Create Index:**
```bash
curl -X POST http://localhost:8080/api/indexes/my-documents \
  -H "Content-Type: application/json"
```

**Check if Index Exists:**
```bash
curl -X GET http://localhost:8080/api/indexes/my-documents/exists
```

**Delete Index:**
```bash
curl -X DELETE http://localhost:8080/api/indexes/my-documents
```

### 6. Health Check

```bash
curl -X GET http://localhost:8080/actuator/health
```

## Common Issues and Solutions

### Issue 1: Bad Request (400)
**Cause**: Missing `Content-Type: application/json` header or malformed JSON
**Solution**: Always include the header and validate JSON syntax

### Issue 2: Validation Errors
**Cause**: Missing required fields (content, indexName)
**Solution**: Ensure all required fields are present:
- `content` or `csvContent`
- `indexName`
- `contentColumnName` (for CSV uploads)

### Issue 3: OpenSearch Connection Errors
**Cause**: OpenSearch not running on localhost:30920
**Solution**: Start OpenSearch or update configuration

### Issue 4: Ollama/Embedding Errors  
**Cause**: Ollama not running or model not available
**Solution**: Start Ollama with llama2 model

## Testing Your Setup

1. **Test Service Health:**
```bash
curl http://localhost:8080/actuator/health
```

2. **Test Simple Document Upload:**
```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test document",
    "indexName": "test-index"
  }'
```

3. **Test CSV Upload:**
```bash
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d '{
    "csvContent": "title,content\n\"Test\",\"Test content\"",
    "indexName": "test-index",
    "contentColumnName": "content"
  }'
```

## Working with Example CSV Files

To use the provided example CSV files, you'll need to properly escape them. The easiest approach is to create JSON files:

```bash
# Create a proper JSON request file
cat > tech-articles-request.json << 'EOF'
{
  "csvContent": "title,content,category,author,publication_date\n\"Introduction to Machine Learning\",\"Machine Learning is a subset of artificial intelligence that enables computers to learn and make decisions without being explicitly programmed. It uses algorithms to analyze data, identify patterns, and make predictions or decisions based on the information it processes.\",\"Technology\",\"Alice Johnson\",\"2024-01-15\"",
  "indexName": "tech-articles",
  "contentColumnName": "content",
  "source": "tech-articles-csv"
}
EOF

# Use it
curl -X POST http://localhost:8080/api/rag/documents/csv \
  -H "Content-Type: application/json" \
  -d @tech-articles-request.json
```

Remember: The key to successful API calls is including the proper headers and well-formed JSON payloads!
