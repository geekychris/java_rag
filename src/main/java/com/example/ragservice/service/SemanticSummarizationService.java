package com.example.ragservice.service;

import com.example.ragservice.dto.SemanticSummarizationRequest;
import com.example.ragservice.dto.SemanticSummarizationResponse;
import com.example.ragservice.dto.SummarizationRequest;
import com.example.ragservice.dto.SummarizationResponse;
import com.example.ragservice.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that performs semantic (vector/hybrid) search and then summarizes results.
 */
@Service
public class SemanticSummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticSummarizationService.class);

    private final VectorStoreService vectorStoreService;
    private final SummarizationService summarizationService;

    @Autowired
    public SemanticSummarizationService(VectorStoreService vectorStoreService,
                                        SummarizationService summarizationService) {
        this.vectorStoreService = vectorStoreService;
        this.summarizationService = summarizationService;
    }

    public SemanticSummarizationResponse searchAndSummarize(SemanticSummarizationRequest request) {
        long totalStart = System.currentTimeMillis();
        SemanticSummarizationResponse response = new SemanticSummarizationResponse(request.getQuery(), request.getIndexName());
        response.setSearchType(request.getSearchType().name());

        try {
            // 1) Search
            long searchStart = System.currentTimeMillis();
            List<SearchResult> results = switch (request.getSearchType()) {
                case HYBRID -> vectorStoreService.hybridSearch(
                        request.getIndexName(), request.getQuery(), request.getMaxResults(), request.getMinScore());
                case VECTOR -> vectorStoreService.searchSimilar(
                        request.getIndexName(), request.getQuery(), request.getMaxResults(), request.getMinScore());
            };
            long searchTime = System.currentTimeMillis() - searchStart;
            response.setSearchTimeMs(searchTime);
            
            // Store results count before potentially clearing results
            int totalResultsCount = results.size();
            
            // Set search results based on request preference
            if (request.getIncludeSearchResults() == Boolean.TRUE) {
                response.setSearchResults(results);
            } else {
                // Don't include search results but preserve the count
                response.setSearchResults(null);
                response.setTotalResults(totalResultsCount);
            }

            if (results.isEmpty()) {
                response.setSuccess(true);
                response.setSummary(null);
                response.setTotalProcessingTimeMs(System.currentTimeMillis() - totalStart);
                return response;
            }

            // 2) Summarize
            long summarizeStart = System.currentTimeMillis();
            SummarizationRequest sumReq = new SummarizationRequest(request.getQuery(), results);
            sumReq.setIncludeSourceReferences(request.getIncludeSourceReferences());
            sumReq.setMaxSummaryLength(request.getMaxSummaryLength());
            sumReq.setCustomPrompt(request.getCustomPrompt());

            SummarizationResponse sumResp = summarizationService.summarize(sumReq);
            long summarizeTime = System.currentTimeMillis() - summarizeStart;
            response.setSummarizationTimeMs(summarizeTime);

            response.setSummary(sumResp.getSummary());
            response.setSourceReferences(sumResp.getSourceReferences());
            response.setModel(sumResp.getModel());
            response.setSuccess(sumResp.isSuccess());

            response.setTotalProcessingTimeMs(System.currentTimeMillis() - totalStart);
            return response;
        } catch (Exception e) {
            logger.error("Semantic search and summarization failed", e);
            response.setSuccess(false);
            response.setError(e.getMessage());
            response.setTotalProcessingTimeMs(System.currentTimeMillis() - totalStart);
            return response;
        }
    }
}

