# CSV Indexing Script Usage Guide

## Overview
The `index_csv_files.sh` script automatically indexes all example CSV files into your RAG service using curl commands.

## Prerequisites
1. **RAG Service Running**: Make sure your RAG service is running on port 8080
2. **Dependencies**: The script requires `curl` and basic shell utilities (available on macOS by default)

## CSV Files to be Indexed

The script will index these files from the `examples/` directory:

### 1. Technology Articles (`tech_articles.csv`)
- **Index Name**: `tech-articles`
- **Content**: 10 technology articles covering ML, microservices, quantum computing, etc.
- **Fields**: title, content, category, author, publication_date

### 2. Research Papers (`research_papers.csv`)
- **Index Name**: `research-papers` 
- **Content**: 10 academic research papers across various fields
- **Fields**: title, content, field, authors, journal

### 3. FAQ Documents (`faq_documents.csv`)
- **Index Name**: `faq-documents`
- **Content**: 10 frequently asked questions and answers
- **Fields**: question, content, category, department

## Usage

### Run the Script
```bash
# Make sure you're in the java_rag directory
cd /path/to/java_rag

# Run the indexing script
./index_csv_files.sh
```

### Expected Output
```
========================================
RAG Service CSV Indexing Script
========================================

[INFO] Checking if RAG server is running on http://localhost:8080...
[SUCCESS] Server is running and healthy

[INFO] Indexing Technology Articles...
[INFO]   File: tech_articles.csv
[INFO]   Index: tech-articles
[INFO]   Content Column: content
[SUCCESS] Successfully indexed 10 documents from Technology Articles
[INFO]   Headers: ["title","content","category","author","publication_date"]

[INFO] Indexing Research Papers...
[INFO]   File: research_papers.csv
[INFO]   Index: research-papers
[INFO]   Content Column: content
[SUCCESS] Successfully indexed 10 documents from Research Papers
[INFO]   Headers: ["title","content","field","authors","journal"]

[INFO] Indexing FAQ Documents...
[INFO]   File: faq_documents.csv
[INFO]   Index: faq-documents
[INFO]   Content Column: content
[SUCCESS] Successfully indexed 10 documents from FAQ Documents
[INFO]   Headers: ["question","content","category","department"]

[SUCCESS] All CSV files have been processed!

[INFO] Running search tests...

[INFO] Testing search in tech-articles with query: 'machine learning'
[SUCCESS] Found 2 results for 'machine learning' in Technology Articles

[INFO] Testing search in research-papers with query: 'quantum computing'
[SUCCESS] Found 2 results for 'quantum computing' in Research Papers

[INFO] Testing search in faq-documents with query: 'password reset'
[SUCCESS] Found 1 results for 'password reset' in FAQ Documents

[SUCCESS] Indexing complete! Your RAG service now contains:
[SUCCESS]   • Technology articles in 'tech-articles' index
[SUCCESS]   • Research papers in 'research-papers' index
[SUCCESS]   • FAQ documents in 'faq-documents' index

[INFO] You can now search these indices using the /api/rag/search endpoint
```

## Testing the Indexed Data

After running the script, you can test the indexed data with these curl commands:

### Search Technology Articles
```bash
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning artificial intelligence",
    "indexName": "tech-articles",
    "size": 3,
    "minScore": 0.0
  }'
```

### Search Research Papers
```bash
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "quantum computing algorithms",
    "indexName": "research-papers", 
    "size": 3,
    "minScore": 0.0
  }'
```

### Search FAQ Documents
```bash
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "password reset login",
    "indexName": "faq-documents",
    "size": 3,
    "minScore": 0.0
  }'
```

### Hybrid Search Example
```bash
curl -X POST http://localhost:8080/api/rag/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "cybersecurity digital threats",
    "indexName": "tech-articles",
    "size": 2,
    "minScore": 0.0
  }'
```

## Troubleshooting

### Server Not Running
If you see:
```
[ERROR] Server is not responding at http://localhost:8080
[ERROR] Please make sure the RAG service is running on port 8080
```

**Solution**: Start your RAG service on port 8080 before running the script.

### CSV Files Not Found
If you see warnings about missing CSV files:
```
[WARNING] tech_articles.csv not found in examples directory
```

**Solution**: Make sure the `examples/` directory contains the CSV files and you're running the script from the correct directory.

### Port Configuration
If your RAG service is running on a different port, edit the script and change:
```bash
RAG_SERVER="http://localhost:8080"
```

## Script Features

- ✅ **Health Check**: Verifies RAG service is running before indexing
- ✅ **Error Handling**: Stops on first error and provides clear error messages  
- ✅ **Progress Tracking**: Colored output shows indexing progress
- ✅ **Automatic Testing**: Runs search tests to verify indexing worked
- ✅ **Flexible Configuration**: Easy to modify server URL and settings
- ✅ **JSON Escaping**: Properly handles CSV content with quotes and newlines

## What Gets Created

After successful indexing, your RAG service will contain:

1. **30 total documents** across 3 indices
2. **Vector embeddings** for semantic search capabilities  
3. **Full-text search** capabilities for hybrid search
4. **Rich metadata** from CSV columns for filtering and display

You can now use these indices for testing, development, or demonstrations of your RAG service capabilities!
