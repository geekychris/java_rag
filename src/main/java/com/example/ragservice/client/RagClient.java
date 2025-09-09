package com.example.ragservice.client;

import com.example.ragservice.client.dto.SearchRequest;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.client.exception.RagClientException;
import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.dto.SummarizationRequest;
import com.example.ragservice.dto.SummarizationResponse;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Client interface for interacting with the RAG service.
 * Provides methods for searching, uploading documents, and managing the vector store.
 */
public interface RagClient extends AutoCloseable {
    
    /**
     * Perform a search query against the RAG service.
     * 
     * @param request the search request containing query, index name, and options
     * @return search response with matching documents and scores
     * @throws RagClientException if the search operation fails
     */
    SearchResponse search(SearchRequest request) throws RagClientException;
    
    /**
     * Convenience method for simple vector search.
     * 
     * @param query the search query
     * @param indexName the name of the index to search
     * @return search response with matching documents and scores
     * @throws RagClientException if the search operation fails
     */
    SearchResponse search(String query, String indexName) throws RagClientException;
    
    /**
     * Convenience method for vector search with limits.
     * 
     * @param query the search query
     * @param indexName the name of the index to search
     * @param maxResults maximum number of results to return
     * @param minScore minimum similarity score threshold
     * @return search response with matching documents and scores
     * @throws RagClientException if the search operation fails
     */
    SearchResponse search(String query, String indexName, int maxResults, double minScore) throws RagClientException;
    
    /**
     * Upload CSV content to be indexed in the RAG service.
     * 
     * @param request the CSV upload request containing content and metadata
     * @return upload response with status and information about indexed documents
     * @throws RagClientException if the upload operation fails
     */
    CsvUploadResponse uploadCsv(CsvUploadRequest request) throws RagClientException;
    
    /**
     * Convenience method for uploading CSV from file.
     * 
     * @param csvFile the CSV file to upload
     * @param indexName the name of the index to store the documents
     * @return upload response with status and information about indexed documents
     * @throws RagClientException if the upload operation fails
     */
    CsvUploadResponse uploadCsvFile(File csvFile, String indexName) throws RagClientException;
    
    /**
     * Convenience method for uploading CSV from file with custom content column.
     * 
     * @param csvFile the CSV file to upload
     * @param indexName the name of the index to store the documents
     * @param contentColumnName the name of the column containing document content
     * @return upload response with status and information about indexed documents
     * @throws RagClientException if the upload operation fails
     */
    CsvUploadResponse uploadCsvFile(File csvFile, String indexName, String contentColumnName) throws RagClientException;
    
    /**
     * Upload a single document to the RAG service.
     * 
     * @param document the document to upload
     * @param indexName the name of the index to store the document
     * @return the ID of the uploaded document
     * @throws RagClientException if the upload operation fails
     */
    String uploadDocument(Document document, String indexName) throws RagClientException;
    
    /**
     * Upload multiple documents to the RAG service.
     * 
     * @param documents the list of documents to upload
     * @param indexName the name of the index to store the documents
     * @return list of document IDs for the uploaded documents
     * @throws RagClientException if the upload operation fails
     */
    List<String> uploadDocuments(List<Document> documents, String indexName) throws RagClientException;
    
    /**
     * Check if the RAG service is healthy and responsive.
     * 
     * @return true if the service is healthy, false otherwise
     */
    boolean isHealthy();
    
    /**
     * Get information about the RAG service.
     * 
     * @return service information as a string
     * @throws RagClientException if the operation fails
     */
    String getServiceInfo() throws RagClientException;
    
    /**
     * Generate a summary based on search results and the original query.
     * 
     * @param request the summarization request containing query and search results
     * @return summarization response with the generated summary
     * @throws RagClientException if the summarization operation fails
     */
    SummarizationResponse summarize(SummarizationRequest request) throws RagClientException;
    
    /**
     * Convenience method for summarizing search results.
     * 
     * @param query the original search query
     * @param searchResults the search results to summarize
     * @return summarization response with the generated summary
     * @throws RagClientException if the summarization operation fails
     */
    SummarizationResponse summarize(String query, List<SearchResult> searchResults) throws RagClientException;
    
    /**
     * Search and summarize in one call - convenience method.
     * 
     * @param query the search query
     * @param indexName the name of the index to search
     * @return a map containing both search results and summary
     * @throws RagClientException if the operation fails
     */
    Map<String, Object> searchAndSummarize(String query, String indexName) throws RagClientException;
    
    /**
     * Search and summarize in one call with parameters.
     * 
     * @param query the search query
     * @param indexName the name of the index to search
     * @param maxResults maximum number of results to return
     * @param minScore minimum similarity score threshold
     * @return a map containing both search results and summary
     * @throws RagClientException if the operation fails
     */
    Map<String, Object> searchAndSummarize(String query, String indexName, int maxResults, double minScore) throws RagClientException;
    
    /**
     * Close the client and release any resources.
     */
    void close();
}
