package com.example.ragservice.service;

import com.example.ragservice.model.Document;
import com.example.ragservice.model.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VectorStoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);
    
    private final RestHighLevelClient client;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;
    
    @Value("${rag.embedding-dimension:4096}")
    private int embeddingDimension;
    
    @Autowired
    public VectorStoreService(RestHighLevelClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Create an index for storing documents with vector embeddings
     */
    public void createIndex(String indexName) throws IOException {
        if (indexExists(indexName)) {
            logger.info("Index {} already exists", indexName);
            return;
        }
        
        // Create index mapping with k-NN vector field
        String mapping = """
            {
              "settings": {
                "index": {
                  "knn": true,
                  "knn.algo_param.ef_search": 100
                }
              },
              "mappings": {
                "properties": {
                  "id": {
                    "type": "keyword"
                  },
                  "content": {
                    "type": "text",
                    "analyzer": "standard"
                  },
                  "metadata": {
                    "type": "object",
                    "dynamic": true
                  },
                  "embedding": {
                    "type": "knn_vector",
                    "dimension": %d,
                    "method": {
                      "name": "hnsw",
                      "space_type": "cosinesimil",
                      "engine": "lucene",
                      "parameters": {
                        "ef_construction": 128,
                        "m": 24
                      }
                    }
                  },
                  "timestamp": {
                    "type": "date"
                  },
                  "source": {
                    "type": "keyword"
                  }
                }
              }
            }
            """.formatted(embeddingDimension);
        
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.source(mapping, XContentType.JSON);
        
        client.indices().create(request, RequestOptions.DEFAULT);
        logger.info("Created index: {}", indexName);
    }
    
    /**
     * Check if an index exists
     */
    public boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }
    
    /**
     * Delete an index
     */
    public void deleteIndex(String indexName) throws IOException {
        if (!indexExists(indexName)) {
            logger.info("Index {} does not exist", indexName);
            return;
        }
        
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        client.indices().delete(request, RequestOptions.DEFAULT);
        logger.info("Deleted index: {}", indexName);
    }
    
    /**
     * Store a document with its embedding in the vector store
     */
    public String storeDocument(String indexName, Document document) throws IOException {
        // Generate embedding if not already present
        if (document.getEmbedding() == null || document.getEmbedding().isEmpty()) {
            logger.debug("Generating embedding for document: {}", document.getId());
            List<Double> embedding = embeddingService.generateEmbedding(document.getContent());
            document.setEmbedding(embedding);
        }
        
        // Ensure index exists
        createIndex(indexName);
        
        // Prepare document for indexing
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", document.getId());
        jsonMap.put("content", document.getContent());
        jsonMap.put("metadata", document.getMetadata());
        jsonMap.put("embedding", document.getEmbedding());
        jsonMap.put("timestamp", document.getTimestamp());
        jsonMap.put("source", document.getSource());
        
        IndexRequest request = new IndexRequest(indexName)
            .id(document.getId())
            .source(jsonMap);
        
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        logger.debug("Stored document {} in index {}", document.getId(), indexName);
        
        return response.getId();
    }
    
    /**
     * Store multiple documents in batch
     */
    public void storeDocuments(String indexName, List<Document> documents) throws IOException {
        logger.info("Storing {} documents in index {}", documents.size(), indexName);
        
        for (Document document : documents) {
            try {
                storeDocument(indexName, document);
            } catch (Exception e) {
                logger.error("Failed to store document {}: {}", document.getId(), e.getMessage());
            }
        }
        
        logger.info("Completed storing documents in index {}", indexName);
    }
    
    /**
     * Retrieve a document by ID
     */
    public Document getDocument(String indexName, String documentId) throws IOException {
        GetRequest request = new GetRequest(indexName, documentId);
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        
        if (!response.isExists()) {
            return null;
        }
        
        return mapSourceToDocument(response.getSourceAsMap());
    }
    
    /**
     * Perform vector similarity search using native k-NN
     */
    public List<SearchResult> searchSimilar(String indexName, String query, int size, double minScore) throws IOException {
        // Generate embedding for the query
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        
        // Convert Double list to float array for k-NN query
        float[] vector = new float[queryEmbedding.size()];
        for (int i = 0; i < queryEmbedding.size(); i++) {
            vector[i] = queryEmbedding.get(i).floatValue();
        }
        
        // Build native k-NN query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // Use native k-NN query which is more efficient and reliable
        Map<String, Object> knnQuery = new HashMap<>();
        Map<String, Object> embeddingField = new HashMap<>();
        embeddingField.put("vector", vector);
        embeddingField.put("k", size);
        knnQuery.put("embedding", embeddingField);
        
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("knn", knnQuery);
        
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(objectMapper.writeValueAsString(queryMap)));
        searchSourceBuilder.size(size);
        searchSourceBuilder.minScore((float) minScore);
        
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        
        List<SearchResult> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Document document = mapSourceToDocument(hit.getSourceAsMap());
            SearchResult result = new SearchResult(document, hit.getScore());
            results.add(result);
        }
        
        logger.debug("Found {} similar documents for query", results.size());
        return results;
    }
    
    /**
     * Perform hybrid search (vector + text) using native k-NN and text search
     */
    public List<SearchResult> hybridSearch(String indexName, String query, int size, double minScore) throws IOException {
        // For hybrid search, we'll use a bool query with both text matching and k-NN
        // This is a simplified version - for true hybrid search you might want to use 
        // separate queries and combine results with custom scoring
        
        // Generate embedding for the query
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        
        // Convert to float array
        float[] vector = new float[queryEmbedding.size()];
        for (int i = 0; i < queryEmbedding.size(); i++) {
            vector[i] = queryEmbedding.get(i).floatValue();
        }
        
        // Build hybrid search using bool query with text match and k-NN
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // Create k-NN query structure
        Map<String, Object> knnQuery = new HashMap<>();
        Map<String, Object> embeddingField = new HashMap<>();
        embeddingField.put("vector", vector);
        embeddingField.put("k", size * 2); // Get more k-NN results for hybrid
        knnQuery.put("embedding", embeddingField);
        
        // Combine with text search in a bool query
        searchSourceBuilder.query(
            QueryBuilders.boolQuery()
                .should(
                    QueryBuilders.matchQuery("content", query)
                        .boost(0.3f)
                )
                .should(
                    QueryBuilders.wrapperQuery(
                        objectMapper.writeValueAsString(Map.of("knn", knnQuery))
                    ).boost(0.7f)
                )
        );
        
        searchSourceBuilder.size(size);
        searchSourceBuilder.minScore((float) minScore);
        
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        
        List<SearchResult> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Document document = mapSourceToDocument(hit.getSourceAsMap());
            SearchResult result = new SearchResult(document, hit.getScore());
            results.add(result);
        }
        
        logger.debug("Found {} documents using hybrid search", results.size());
        return results;
    }
    
    /**
     * Map OpenSearch source to Document object
     */
    @SuppressWarnings("unchecked")
    private Document mapSourceToDocument(Map<String, Object> source) {
        Document document = new Document();
        document.setId((String) source.get("id"));
        document.setContent((String) source.get("content"));
        document.setMetadata((Map<String, Object>) source.get("metadata"));
        document.setSource((String) source.get("source"));
        
        if (source.get("timestamp") != null) {
            document.setTimestamp(java.time.Instant.parse(source.get("timestamp").toString()));
        }
        
        if (source.get("embedding") != null) {
            List<Double> embedding = (List<Double>) source.get("embedding");
            document.setEmbedding(embedding);
        }
        
        return document;
    }
}
