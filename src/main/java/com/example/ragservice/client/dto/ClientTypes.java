package com.example.ragservice.client.dto;

import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;

/**
 * Client-specific type aliases for response objects.
 * This allows the client library to use the same response types as the service
 * while maintaining clean separation.
 */
public class ClientTypes {
    
    // Re-export service response types for client use
    public static class ClientSearchResponse extends SearchResponse {}
    public static class ClientCsvUploadResponse extends CsvUploadResponse {}
    public static class ClientDocument extends Document {}
    public static class ClientSearchResult extends SearchResult {}
    
    // Utility methods for type conversion if needed
    public static SearchResponse toSearchResponse(ClientSearchResponse clientResponse) {
        return clientResponse;
    }
    
    public static ClientSearchResponse fromSearchResponse(SearchResponse serviceResponse) {
        ClientSearchResponse clientResponse = new ClientSearchResponse();
        clientResponse.setQuery(serviceResponse.getQuery());
        clientResponse.setResults(serviceResponse.getResults());
        clientResponse.setTotalResults(serviceResponse.getTotalResults());
        clientResponse.setSuccess(serviceResponse.isSuccess());
        return clientResponse;
    }
}
