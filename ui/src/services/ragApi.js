import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // Ensure CORS is handled properly
});

// Add request interceptor for logging
api.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`);
    return config;
  },
  (error) => Promise.reject(error)
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
      console.error('Network Error - Service may be offline:', error.message);
      error.isNetworkError = true;
    } else {
      console.error('API Error:', error.response?.data || error.message);
    }
    return Promise.reject(error);
  }
);

export const ragApi = {
  // Health check
  healthCheck: async () => {
    const response = await api.get('/actuator/health');
    return response.data;
  },

  // Get available indexes
  getIndexes: async () => {
    try {
      const response = await api.get('/api/indexes');
      return response.data.indexes || response.data;
    } catch (error) {
      // Fallback: return common index names if endpoint doesn't exist
      console.warn('Indexes endpoint not available, using fallback');
      return ['summarization-test', 'documents', 'knowledge-base'];
    }
  },

  // Create a new index
  createIndex: async (indexName) => {
    const response = await api.post(`/api/indexes/${indexName}`);
    return response.data;
  },

  // Delete an index
  deleteIndex: async (indexName) => {
    const response = await api.delete(`/api/indexes/${indexName}`);
    return response.data;
  },

  // Check if an index exists
  indexExists: async (indexName) => {
    const response = await api.get(`/api/indexes/${indexName}/exists`);
    return response.data;
  },

  // Upload CSV data to an index
  uploadCsv: async (indexName, csvContent, options = {}) => {
    const {
      contentColumnName = 'content',
      source = 'csv-upload',
    } = options;

    const response = await api.post('/api/rag/documents/csv', {
      indexName,
      csvContent,
      contentColumnName,
      source,
    });
    return response.data;
  },

  // Search documents
  searchDocuments: async (query, indexName, options = {}) => {
    const {
      size = 10,
      minScore = 0.01,
      includeEmbeddings = false,
    } = options;

    const response = await api.post('/api/rag/search', {
      query,
      indexName,
      size,
      minScore,
      includeEmbeddings,
    });
    return response.data;
  },

  // Search with hybrid approach
  searchHybrid: async (query, indexName, options = {}) => {
    const {
      size = 10,
      minScore = 0.01,
      includeEmbeddings = false,
    } = options;

    const response = await api.post('/api/rag/search/hybrid', {
      query,
      indexName,
      size,
      minScore,
      includeEmbeddings,
    });
    return response.data;
  },

  // Simple query summarization (recommended approach)
  summarizeQuery: async (query, indexName, options = {}) => {
    const {
      maxResults = 10,
      minScore = 0.01,
      maxSummaryLength,
      customPrompt,
    } = options;

    const requestBody = {
      query,
      indexName,
      maxResults,
      minScore,
    };

    if (maxSummaryLength) requestBody.maxSummaryLength = maxSummaryLength;
    if (customPrompt) requestBody.customPrompt = customPrompt;

    const response = await api.post('/api/rag/summarize-query', requestBody);
    return response.data;
  },

  // Advanced semantic summarization
  semanticSummarize: async (query, indexName, options = {}) => {
    const {
      maxResults = 10,
      minScore = 0.01,
      searchType = 'VECTOR',
      maxSummaryLength,
      includeSourceReferences = true,
      includeSearchResults = false,
      customPrompt,
    } = options;

    const requestBody = {
      query,
      indexName,
      maxResults,
      minScore,
      searchType,
      includeSourceReferences,
      includeSearchResults,
    };

    if (maxSummaryLength) requestBody.maxSummaryLength = maxSummaryLength;
    if (customPrompt) requestBody.customPrompt = customPrompt;

    const response = await api.post('/api/rag/semantic-summarize', requestBody);
    return response.data;
  },

  // Legacy search and summarize
  searchAndSummarize: async (query, indexName, options = {}) => {
    const {
      size = 10,
      minScore = 0.01,
    } = options;

    const response = await api.post('/api/rag/search-and-summarize', {
      query,
      indexName,
      size,
      minScore,
    });
    return response.data;
  },
};

export default ragApi;
