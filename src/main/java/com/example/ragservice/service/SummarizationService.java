package com.example.ragservice.service;

import com.example.ragservice.dto.SummarizationRequest;
import com.example.ragservice.dto.SummarizationResponse;
import com.example.ragservice.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating AI-powered summaries of search results.
 * Uses configurable LLM models to create concise, relevant summaries.
 */
@Service
@ConditionalOnProperty(name = "rag.summarization.enabled", havingValue = "true", matchIfMissing = true)
public class SummarizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SummarizationService.class);
    
    private final ChatClient chatClient;
    
    @Value("${rag.summarization.model:llama2}")
    private String summarizationModel;
    
    @Value("${rag.summarization.max-input-tokens:8000}")
    private int maxInputTokens;
    
    @Value("${rag.summarization.max-output-tokens:1000}")
    private int maxOutputTokens;
    
    @Value("${rag.summarization.temperature:0.3}")
    private double temperature;
    
    @Value("${rag.summarization.system-prompt:You are an AI assistant that provides concise, accurate summaries based on search results and user queries.}")
    private String systemPrompt;
    
    private static final String SUMMARIZATION_TEMPLATE = """
            Based on the user query: "{query}"
            
            Please provide a comprehensive summary using the following search results:
            
            {searchResults}
            
            {customInstructions}
            
            Guidelines:
            - Create a coherent summary that directly addresses the user's query
            - Include the most relevant information from the search results
            - Maintain accuracy and avoid making up information not present in the sources
            - Keep the summary concise but informative
            {sourceInstructions}
            """;
    
    @Autowired
    public SummarizationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * Generate a summary based on search results and the original query.
     *
     * @param request The summarization request containing query and search results
     * @return SummarizationResponse with the generated summary
     */
    public SummarizationResponse summarize(SummarizationRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting summarization for query: '{}' with {} search results", 
                       request.getQuery(), request.getSearchResults().size());
            
            // Validate and prepare input
            if (request.getSearchResults().isEmpty()) {
                return createErrorResponse(request.getQuery(), "No search results provided for summarization", startTime);
            }
            
            // Prepare search results text
            String searchResultsText = formatSearchResults(request.getSearchResults());
            
            // Check token limits (rough estimation)
            if (estimateTokens(searchResultsText + request.getQuery()) > maxInputTokens) {
                logger.warn("Input too large, truncating search results for summarization");
                searchResultsText = truncateSearchResults(request.getSearchResults(), request.getQuery());
            }
            
            // Build the prompt
            String customInstructions = buildCustomInstructions(request);
            String sourceInstructions = request.getIncludeSourceReferences() == Boolean.TRUE ? 
                "- Include references to specific sources when relevant" : "";
            
            PromptTemplate promptTemplate = new PromptTemplate(SUMMARIZATION_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of(
                "query", request.getQuery(),
                "searchResults", searchResultsText,
                "customInstructions", customInstructions,
                "sourceInstructions", sourceInstructions
            ));
            
            // Add system message
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.add(prompt.getInstructions().get(0));
            
            Prompt finalPrompt = new Prompt(messages);
            
            // Call the LLM
            logger.debug("Calling LLM model '{}' for summarization", summarizationModel);
            ChatResponse response = chatClient.call(finalPrompt);
            
            String summary = response.getResult().getOutput().getContent();
            
            // Create successful response
            SummarizationResponse result = new SummarizationResponse(
                request.getQuery(),
                summary,
                true,
                request.getSearchResults().size()
            );
            
            result.setModel(summarizationModel);
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            if (request.getIncludeSourceReferences() == Boolean.TRUE) {
                result.setSourceReferences(extractSourceReferences(request.getSearchResults()));
            }
            
            logger.info("Successfully generated summary for query '{}' in {}ms", 
                       request.getQuery(), result.getProcessingTimeMs());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to generate summary for query: '{}'", request.getQuery(), e);
            return createErrorResponse(request.getQuery(), "Failed to generate summary: " + e.getMessage(), startTime);
        }
    }
    
    private String formatSearchResults(List<SearchResult> searchResults) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            sb.append(String.format("[Result %d - Score: %.3f]\n", i + 1, result.getScore()));
            sb.append("Content: ").append(result.getDocument().getContent()).append("\n");
            
            if (result.getDocument().getMetadata() != null && !result.getDocument().getMetadata().isEmpty()) {
                sb.append("Metadata: ").append(result.getDocument().getMetadata()).append("\n");
            }
            
            if (result.getDocument().getSource() != null) {
                sb.append("Source: ").append(result.getDocument().getSource()).append("\n");
            }
            
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private String truncateSearchResults(List<SearchResult> searchResults, String query) {
        // Simple truncation strategy: take the highest scoring results that fit within token limit
        List<SearchResult> truncated = new ArrayList<>();
        int estimatedTokens = estimateTokens(query);
        
        for (SearchResult result : searchResults) {
            String resultText = formatSingleResult(result);
            int resultTokens = estimateTokens(resultText);
            
            if (estimatedTokens + resultTokens < maxInputTokens * 0.8) { // Leave some buffer
                truncated.add(result);
                estimatedTokens += resultTokens;
            } else {
                break;
            }
        }
        
        logger.info("Truncated search results from {} to {} items due to token limits", 
                   searchResults.size(), truncated.size());
        
        return formatSearchResults(truncated);
    }
    
    private String formatSingleResult(SearchResult result) {
        return String.format("Content: %s\nMetadata: %s\nSource: %s\n", 
                           result.getDocument().getContent(),
                           result.getDocument().getMetadata(),
                           result.getDocument().getSource());
    }
    
    private String buildCustomInstructions(SummarizationRequest request) {
        StringBuilder instructions = new StringBuilder();
        
        if (request.getMaxSummaryLength() != null) {
            instructions.append(String.format("- Limit the summary to approximately %d words\n", 
                                            request.getMaxSummaryLength()));
        }
        
        if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
            instructions.append("- Additional instructions: ")
                        .append(request.getCustomPrompt())
                        .append("\n");
        }
        
        return instructions.toString();
    }
    
    private List<String> extractSourceReferences(List<SearchResult> searchResults) {
        return searchResults.stream()
                .map(result -> result.getDocument().getSource())
                .filter(source -> source != null && !source.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    private int estimateTokens(String text) {
        // Rough estimation: 1 token ≈ 4 characters for English text
        return Math.max(1, text.length() / 4);
    }
    
    private SummarizationResponse createErrorResponse(String query, String error, long startTime) {
        SummarizationResponse response = new SummarizationResponse(query, null, false, 0);
        response.setSummary("Error: " + error);
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        return response;
    }
}
