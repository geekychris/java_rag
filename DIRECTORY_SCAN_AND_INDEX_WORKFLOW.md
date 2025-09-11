# Directory Scan and CSV Indexing Workflow

This document provides complete curl command examples for scanning directories to build CSV files and then indexing those CSV files using the streaming indexing endpoint.

## Prerequisites

- RAG service running on `http://localhost:8080`
- Directory with documents to scan
- Sufficient disk space for CSV output files

## Step 1: Directory Scanning to CSV

### Basic Directory Scan

```bash
# Scan a directory and generate CSV with extracted document content
curl -X POST http://localhost:8080/api/v1/document-processing/directory-scan \
  -H "Content-Type: application/json" \
  -d '{
    "directory_path": "/path/to/your/documents",
    "output_csv_path": "/path/to/output/documents.csv",
    "recursive": true,
    "supported_extensions": ["txt", "pdf", "doc", "docx", "rtf", "html"],
    "max_files": 1000
  }'
```

### Directory Scan with Specific Configuration

```bash
# Example: Scan retro computer documents directory
curl -X POST http://localhost:8080/api/v1/document-processing/directory-scan \
  -H "Content-Type: application/json" \
  -d '{
    "directory_path": "/Users/chris/Documents/retro_computer",
    "output_csv_path": "/Users/chris/Documents/retro_docs.csv",
    "recursive": true,
    "supported_extensions": ["txt", "pdf", "doc", "docx"],
    "max_files": 100
  }'
```

### Monitor Scan Progress

```bash
# Check scan status (replace SCAN_ID with actual scan ID from previous response)
curl -s http://localhost:8080/api/v1/document-processing/directory-scan/SCAN_ID

# Example with actual scan ID:
curl -s http://localhost:8080/api/v1/document-processing/directory-scan/scan_1757486552722_7c538ff0
```

### List Active Scans

```bash
# View all active scan operations
curl -s http://localhost:8080/api/v1/document-processing/directory-scan/active
```

### Cancel a Scan

```bash
# Cancel an active scan (replace SCAN_ID with actual scan ID)
curl -X DELETE http://localhost:8080/api/v1/document-processing/directory-scan/SCAN_ID
```

## Step 2: CSV File Indexing (Streaming)

### Basic CSV Indexing

```bash
# Index a CSV file using streaming ingestion
curl -X POST http://localhost:8080/api/rag/documents/csv/file \
  -H "Content-Type: application/json" \
  -d '{
    "csvFilePath": "/path/to/your/documents.csv",
    "indexName": "my-document-index",
    "contentColumnName": "text",
    "source": "my_document_collection",
    "batchSize": 50,
    "maxRecords": 1000
  }'
```

### Large File CSV Indexing

```bash
# Index a large CSV file (e.g., 174MB docs.csv)
curl -X POST http://localhost:8080/api/rag/documents/csv/file \
  -H "Content-Type: application/json" \
  -d '{
    "csvFilePath": "/Users/chris/Documents/docs.csv",
    "indexName": "retro-docs",
    "contentColumnName": "text",
    "source": "retro_computer_collection",
    "batchSize": 100,
    "maxRecords": 10000
  }'
```

### CSV Indexing with Document ID Column

```bash
# Index CSV with custom document ID column
curl -X POST http://localhost:8080/api/rag/documents/csv/file \
  -H "Content-Type: application/json" \
  -d '{
    "csvFilePath": "/path/to/documents.csv",
    "indexName": "my-docs-with-ids",
    "contentColumnName": "text",
    "docIdColumnName": "document_id",
    "source": "document_collection",
    "batchSize": 50
  }'
```

## Step 3: Search and Verify

### Search the Indexed Documents

```bash
# Search the newly indexed documents
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "indexName": "retro-docs",
    "query": "Amiga computer",
    "size": 5,
    "minScore": 0.1,
    "includeEmbeddings": false
  }'
```

### Hybrid Search

```bash
# Use hybrid search (vector + text)
curl -X POST http://localhost:8080/api/rag/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "indexName": "retro-docs",
    "query": "laser engraving machine installation",
    "size": 10,
    "minScore": 0.1
  }'
```

## Complete Workflow Example

Here's a complete example workflow from directory scan to search:

```bash
# 1. Start directory scan
SCAN_RESPONSE=$(curl -X POST http://localhost:8080/api/v1/document-processing/directory-scan \
  -H "Content-Type: application/json" \
  -d '{
    "directory_path": "/Users/chris/Documents/retro_computer",
    "output_csv_path": "/Users/chris/Documents/retro_complete.csv",
    "recursive": true,
    "supported_extensions": ["txt", "pdf", "doc", "docx"],
    "max_files": 500
  }')

echo "Scan started: $SCAN_RESPONSE"

# Extract scan ID from response (requires jq)
SCAN_ID=$(echo $SCAN_RESPONSE | jq -r '.scan_id')
echo "Scan ID: $SCAN_ID"

# 2. Monitor scan progress
echo "Monitoring scan progress..."
while true; do
    STATUS=$(curl -s http://localhost:8080/api/v1/document-processing/directory-scan/$SCAN_ID | jq -r '.status')
    echo "Status: $STATUS"
    if [[ "$STATUS" == "COMPLETED" || "$STATUS" == "FAILED" ]]; then
        break
    fi
    sleep 10
done

# 3. Index the generated CSV
echo "Starting CSV indexing..."
INDEX_RESPONSE=$(curl -X POST http://localhost:8080/api/rag/documents/csv/file \
  -H "Content-Type: application/json" \
  -d '{
    "csvFilePath": "/Users/chris/Documents/retro_complete.csv",
    "indexName": "retro-complete-docs",
    "contentColumnName": "text",
    "source": "retro_computer_complete_scan",
    "batchSize": 100,
    "maxRecords": 5000
  }')

echo "Indexing response: $INDEX_RESPONSE"

# 4. Test search
echo "Testing search..."
curl -X POST http://localhost:8080/api/rag/search \
  -H "Content-Type: application/json" \
  -d '{
    "indexName": "retro-complete-docs",
    "query": "computer manual",
    "size": 3,
    "minScore": 0.1,
    "includeEmbeddings": false
  }' | jq '.'
```

## Request Parameters

### Directory Scan Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `directory_path` | string | Yes | Full path to directory to scan |
| `output_csv_path` | string | No | Output CSV file path (auto-generated if not provided) |
| `recursive` | boolean | No | Scan subdirectories recursively (default: true) |
| `supported_extensions` | array | No | File extensions to process (default: common document types) |
| `max_files` | integer | No | Maximum number of files to process |

### CSV Indexing Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `csvFilePath` | string | Yes | Full path to CSV file |
| `indexName` | string | Yes | Name of the vector index to create/update |
| `contentColumnName` | string | No | Column containing document text (default: "text") |
| `docIdColumnName` | string | No | Column containing document IDs (default: auto-generate) |
| `source` | string | No | Source identifier for the documents |
| `batchSize` | integer | No | Number of documents per batch (default: 100) |
| `maxRecords` | integer | No | Maximum records to process (for testing) |

## CSV File Format

The generated CSV files have the following structure:

```csv
path,file_name,file_path,file_size,content_type,text,metadata
/full/path/to/file.txt,file.txt,/full/path/to/file.txt,1234,text/plain,"Document content here","{\"extraction_method\":\"direct_read\"}"
```

### Column Descriptions

- **path**: Full file path including filename (used for clickable links in UI)
- **file_name**: Just the filename
- **file_path**: Full file path (legacy)
- **file_size**: File size in bytes
- **content_type**: MIME type of the file
- **text**: Extracted text content from the document
- **metadata**: JSON string with additional file metadata

## File Viewer Integration

When documents are indexed with file paths, they become clickable in the search results UI. The file viewer uses these endpoints:

```bash
# Get file information
curl -s "http://localhost:8080/api/v1/file-viewer/info?path=BASE64_ENCODED_PATH"

# Get file content
curl -s "http://localhost:8080/api/v1/file-viewer/content?path=BASE64_ENCODED_PATH"
```

Example:
```bash
# Base64 encode the file path
ENCODED_PATH=$(echo -n "/Users/chris/Documents/example.pdf" | base64)

# Get file info
curl -s "http://localhost:8080/api/v1/file-viewer/info?path=$ENCODED_PATH"
```

## Error Handling

### Common Error Responses

```json
{
  "success": false,
  "error": "Directory path is required"
}
```

```json
{
  "success": false,
  "error": "CSV file not found: /path/to/missing.csv"
}
```

### Monitoring and Troubleshooting

1. **Check scan status regularly** during long-running operations
2. **Monitor CSV file size** to ensure content is being written
3. **Use small test batches** before processing large datasets
4. **Check server logs** for detailed error information

## Performance Considerations

- **Batch Size**: Larger batches (100-500) are more efficient for indexing
- **Max Files**: Use limits for testing; remove for full scans
- **Supported Extensions**: Limit to needed file types to improve performance
- **OCR Disabled**: PDFs with many images may be slow; OCR is disabled by default
- **Timeout**: 2-minute timeout per file prevents hanging on problematic files

## Health Check

Before starting, verify the service is running:

```bash
curl -s http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP","components":{"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```
