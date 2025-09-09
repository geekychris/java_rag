# Semantic Summarization Examples

This document demonstrates how to use the new semantic summarization functionality that performs vector search and summarization in a single operation.

## Overview

The new semantic summarization system provides:

1. **SemanticSummarizationService** - Performs search internally and then summarizes results
2. **Enhanced Search Response DTOs** - Flexible response formats (full, lightweight, or summary)
3. **New REST Endpoints** - Clean APIs for semantic summarization

## Key Differences

| Feature | Original `/summarize` | New `/semantic-summarize` |
|---------|---------------------|---------------------------|
| Input | Pre-provided search results | Query + search parameters |
| Search | External (manual) | Internal (automatic) |
| Search Types | N/A | Vector or Hybrid |
| Response | Summary only | Search results + summary |
| Performance | Single operation | Search + summarization timing |

## API Endpoints

### 1. Semantic Summarization - `/api/rag/semantic-summarize`

**Request:**
```json
{
  "query": "What is machine learning?",
  "indexName": "knowledge_base",
  "maxResults": 10,
  "minScore": 0.7,
  "searchType": "VECTOR",
  "maxSummaryLength": 200,
  "includeSourceReferences": true,
  "includeSearchResults": false,
  "customPrompt": "Focus on practical applications"
}
```

**Response:**
```json
{
  "query": "What is machine learning?",
  "indexName": "knowledge_base",
  "summary": "Machine learning is a subset of artificial intelligence...",
  "success": true,
  "searchResults": null,
  "totalResults": 5,
  "sourceReferences": ["doc1.txt", "doc2.txt", "doc3.txt"],
  "model": "llama2",
  "searchType": "VECTOR",
  "timestamp": "2024-01-15T10:30:00",
  "searchTimeMs": 150,
  "summarizationTimeMs": 800,
  "totalProcessingTimeMs": 950
}
```

### 2. Enhanced Search Response Types

The new `EnhancedSearchResponse` supports multiple response formats:

#### Full Results (default)
```json
{
  "fullResults": [
    {
      "documentId": "doc123",
      "content": "Complete document content...",
      "metadata": {"author": "John Doe", "date": "2024-01-01"},
      "source": "research_paper.pdf",
      "score": 0.95,
      "embedding": [0.1, 0.2, ...] // optional
    }
  ]
}
```

#### Lightweight Results (IDs + metadata only)
```json
{
  "lightweightResults": [
    {
      "documentId": "doc123",
      "metadata": {"author": "John Doe", "date": "2024-01-01"},
      "source": "research_paper.pdf",
      "score": 0.95
    }
  ]
}
```

#### Document Summaries (snippets)
```json
{
  "documentSummaries": [
    {
      "documentId": "doc123",
      "snippet": "Machine learning is a method of data analysis...",
      "metadata": {"author": "John Doe", "date": "2024-01-01"},
      "source": "research_paper.pdf",
      "score": 0.95,
      "contentLength": 2048
    }
  ]
}
```

## Usage Examples

### Example 1: Basic Semantic Summarization

```bash
curl -X POST http://localhost:8080/api/rag/semantic-summarize \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How does photosynthesis work?",
    "indexName": "biology_documents",
    "maxResults": 5,
    "searchType": "VECTOR"
  }'
```

### Example 2: Hybrid Search with Custom Prompt

```bash
curl -X POST http://localhost:8080/api/rag/semantic-summarize \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Climate change effects on agriculture",
    "indexName": "environmental_research",
    "maxResults": 8,
    "minScore": 0.8,
    "searchType": "HYBRID",
    "maxSummaryLength": 300,
    "customPrompt": "Focus on economic impacts and adaptation strategies",
    "includeSourceReferences": true,
    "includeSearchResults": true
  }'
```

### Example 3: Java Client Usage

```java
// Create request
SemanticSummarizationRequest request = new SemanticSummarizationRequest(
    "What are the benefits of renewable energy?", 
    "energy_documents"
);
request.setMaxResults(10);
request.setSearchType(SemanticSummarizationRequest.SearchType.HYBRID);
request.setIncludeSourceReferences(true);
request.setCustomPrompt("Emphasize environmental and economic benefits");

// Call service
SemanticSummarizationResponse response = semanticSummarizationService.searchAndSummarize(request);

// Process response
if (response.isSuccess()) {
    System.out.println("Summary: " + response.getSummary());
    System.out.println("Found " + response.getTotalResults() + " relevant documents");
    System.out.println("Search took: " + response.getSearchTimeMs() + "ms");
    System.out.println("Summarization took: " + response.getSummarizationTimeMs() + "ms");
}
```

## Performance Considerations

### Timing Breakdown
- **searchTimeMs**: Time spent on vector/hybrid search
- **summarizationTimeMs**: Time spent on LLM summarization  
- **totalProcessingTimeMs**: Total end-to-end processing time

### Optimization Tips

1. **Adjust maxResults**: Lower values reduce summarization time
2. **Set appropriate minScore**: Higher thresholds improve relevance
3. **Use includeSearchResults wisely**: Set to false for faster responses
4. **Choose search type carefully**: 
   - VECTOR: Faster, better for semantic similarity
   - HYBRID: Slower, better for exact keyword matches

## Migration Guide

### From `/search-and-summarize` to `/semantic-summarize`

**Old approach:**
```json
// POST /api/rag/search-and-summarize
{
  "query": "machine learning",
  "indexName": "docs",
  "size": 5,
  "minScore": 0.7
}
```

**New approach:**
```json
// POST /api/rag/semantic-summarize  
{
  "query": "machine learning",
  "indexName": "docs",
  "maxResults": 5,
  "minScore": 0.7,
  "searchType": "VECTOR",
  "includeSearchResults": false
}
```

### Benefits of Migration

1. **Cleaner separation of concerns**: Search logic isolated from summarization
2. **Better performance tracking**: Separate timing for each operation
3. **More flexible responses**: Control what data is returned
4. **Enhanced search options**: Vector vs hybrid search types
5. **Improved error handling**: Better error reporting for each step

## Error Handling

The semantic summarization service provides detailed error information:

```json
{
  "success": false,
  "error": "Index 'non_existent_index' not found",
  "query": "test query",
  "indexName": "non_existent_index",
  "totalProcessingTimeMs": 45,
  "searchTimeMs": 45,
  "summarizationTimeMs": null
}
```

Common error scenarios:
- **Index not found**: Check index name and ensure it exists
- **No search results**: Adjust minScore or search parameters  
- **Summarization failed**: Check LLM service availability
- **Request validation**: Verify required fields are provided

## Configuration

Add to `application.yml`:

```yaml
rag:
  summarization:
    enabled: true
    model: "llama2"
    max-input-tokens: 8000
    max-output-tokens: 1000
    temperature: 0.3
```

## Testing

Run the semantic summarization tests:

```bash
./mvnw test -Dtest=SemanticSummarizationServiceTest
```

## Backwards Compatibility

- Original `/summarize` endpoint unchanged
- Existing `/search-and-summarize` endpoint preserved
- All existing DTOs remain functional
- New functionality is additive, not breaking
