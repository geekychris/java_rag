

# Scan
curl -X POST http://localhost:8080/api/v1/document-processing/directory-scan \
-H "Content-Type: application/json" \
-d '{
"directory_path": "/Users/chris/Documents",
"output_csv_path": "/Users/chris/Documents/docs.csv",
"recursive": true,
"supported_extensions": ["txt", "pdf", "doc", "docx"],
"max_files": 500000
}'


# indexing
curl -X POST http://localhost:8080/api/rag/documents/csv/file \
-H "Content-Type: application/json" \
-d '{
"csvFilePath": "/path/to/output.csv",
"indexName": "my-docs",
"contentColumnName": "text",
"batchSize": 100
}'