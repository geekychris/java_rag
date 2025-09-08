package com.example.ragservice.client.exception;

/**
 * Base exception for RAG client operations.
 */
public class RagClientException extends Exception {
    
    private final int statusCode;
    private final String errorBody;
    
    public RagClientException(String message) {
        super(message);
        this.statusCode = -1;
        this.errorBody = null;
    }
    
    public RagClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.errorBody = null;
    }
    
    public RagClientException(String message, int statusCode, String errorBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }
    
    public RagClientException(String message, int statusCode, String errorBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getErrorBody() {
        return errorBody;
    }
    
    public boolean hasStatusCode() {
        return statusCode != -1;
    }
}
