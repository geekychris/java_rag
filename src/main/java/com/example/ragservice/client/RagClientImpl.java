package com.example.ragservice.client;

import com.example.ragservice.client.config.RagClientConfig;
import com.example.ragservice.client.dto.SearchRequest;
import com.example.ragservice.client.dto.CsvUploadRequest;
import com.example.ragservice.client.exception.RagClientException;
import com.example.ragservice.dto.SearchResponse;
import com.example.ragservice.dto.CsvUploadResponse;
import com.example.ragservice.model.Document;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the RAG client interface using Apache HttpClient.
 */
public class RagClientImpl implements RagClient {
    
    private final RagClientConfig config;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    
    public RagClientImpl(RagClientConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.baseUrl = config.getBaseUrl();
        
        // Configure HTTP client with timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(config.getConnectTimeout()))
                .setResponseTimeout(Timeout.of(config.getReadTimeout()))
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
    
    public RagClientImpl() {
        this(RagClientConfig.defaultConfig());
    }
    
    @Override
    public SearchResponse search(SearchRequest request) throws RagClientException {
        try {
            String url = baseUrl + "/api/rag/search";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            
            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    return objectMapper.readValue(responseBody, SearchResponse.class);
                } else {
                    throw new RagClientException(
                        "Search request failed with status: " + response.getCode(),
                        response.getCode(),
                        responseBody
                    );
                }
            }
        } catch (IOException | ParseException e) {
            throw new RagClientException("Failed to execute search request", e);
        }
    }
    
    @Override
    public SearchResponse search(String query, String indexName) throws RagClientException {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .indexName(indexName)
                .build();
        return search(request);
    }
    
    @Override
    public SearchResponse search(String query, String indexName, int maxResults, double minScore) throws RagClientException {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .indexName(indexName)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        return search(request);
    }
    
    @Override
    public CsvUploadResponse uploadCsv(CsvUploadRequest request) throws RagClientException {
        try {
            String url = baseUrl + "/api/rag/documents/csv";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            
            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    return objectMapper.readValue(responseBody, CsvUploadResponse.class);
                } else {
                    throw new RagClientException(
                        "CSV upload request failed with status: " + response.getCode(),
                        response.getCode(),
                        responseBody
                    );
                }
            }
        } catch (IOException | ParseException e) {
            throw new RagClientException("Failed to execute CSV upload request", e);
        }
    }
    
    @Override
    public CsvUploadResponse uploadCsvFile(File csvFile, String indexName) throws RagClientException {
        return uploadCsvFile(csvFile, indexName, "content");
    }
    
    @Override
    public CsvUploadResponse uploadCsvFile(File csvFile, String indexName, String contentColumnName) throws RagClientException {
        try {
            String csvContent = Files.readString(csvFile.toPath());
            
            CsvUploadRequest request = CsvUploadRequest.builder()
                    .csvContent(csvContent)
                    .indexName(indexName)
                    .contentColumnName(contentColumnName)
                    .source("client-file-upload")
                    .build();
            
            return uploadCsv(request);
        } catch (IOException e) {
            throw new RagClientException("Failed to read CSV file: " + csvFile.getName(), e);
        }
    }
    
    @Override
    public String uploadDocument(Document document, String indexName) throws RagClientException {
        try {
            String url = baseUrl + "/api/rag/documents/" + indexName;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            
            String requestBody = objectMapper.writeValueAsString(document);
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    // Parse response to get document ID
                    Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    return (String) result.get("documentId");
                } else {
                    throw new RagClientException(
                        "Document upload request failed with status: " + response.getCode(),
                        response.getCode(),
                        responseBody
                    );
                }
            }
        } catch (IOException | ParseException e) {
            throw new RagClientException("Failed to execute document upload request", e);
        }
    }
    
    @Override
    public List<String> uploadDocuments(List<Document> documents, String indexName) throws RagClientException {
        try {
            String url = baseUrl + "/api/rag/documents/" + indexName + "/batch";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            
            String requestBody = objectMapper.writeValueAsString(documents);
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    // Parse response to get document IDs
                    Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    return (List<String>) result.get("documentIds");
                } else {
                    throw new RagClientException(
                        "Batch document upload request failed with status: " + response.getCode(),
                        response.getCode(),
                        responseBody
                    );
                }
            }
        } catch (IOException | ParseException e) {
            throw new RagClientException("Failed to execute batch document upload request", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            String url = baseUrl + "/actuator/health";
            HttpGet httpGet = new HttpGet(url);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return response.getCode() == 200;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getServiceInfo() throws RagClientException {
        try {
            String url = baseUrl + "/actuator/info";
            HttpGet httpGet = new HttpGet(url);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    return responseBody;
                } else {
                    throw new RagClientException(
                        "Service info request failed with status: " + response.getCode(),
                        response.getCode(),
                        responseBody
                    );
                }
            }
        } catch (IOException | ParseException e) {
            throw new RagClientException("Failed to get service info", e);
        }
    }
    
    @Override
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            // Log error but don't throw exception in close method
            System.err.println("Error closing HTTP client: " + e.getMessage());
        }
    }
    
    // Factory method for easy client creation
    public static RagClient create() {
        return new RagClientImpl();
    }
    
    public static RagClient create(String baseUrl) {
        RagClientConfig config = RagClientConfig.builder()
                .baseUrl(baseUrl)
                .build();
        return new RagClientImpl(config);
    }
    
    public static RagClient create(RagClientConfig config) {
        return new RagClientImpl(config);
    }
}
