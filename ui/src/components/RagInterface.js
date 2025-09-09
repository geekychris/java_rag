import React, { useState, useEffect } from 'react';
import { Search, Database, Settings, Sparkles, List, Brain, AlertCircle, CheckCircle2 } from 'lucide-react';
import SearchResults from './SearchResults';
import Summarization from './Summarization';
import ragApi from '../services/ragApi';

const RagInterface = () => {
  // State management
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState('summarization-test');
  const [mode, setMode] = useState('summarize'); // 'search' or 'summarize'
  const [searchType, setSearchType] = useState('vector'); // 'vector' or 'hybrid'
  const [maxResults, setMaxResults] = useState(10);
  const [minScore, setMinScore] = useState(0.01);
  const [customPrompt, setCustomPrompt] = useState('');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [showVectors, setShowVectors] = useState(false);
  
  // Results state
  const [searchResults, setSearchResults] = useState(null);
  const [summarizationResult, setSummarizationResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // App state
  const [availableIndexes, setAvailableIndexes] = useState([]);
  const [serviceStatus, setServiceStatus] = useState('unknown');

  // Load available indexes and check service health on component mount
  useEffect(() => {
    loadIndexes();
    checkServiceHealth();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const checkServiceHealth = async () => {
    try {
      await ragApi.healthCheck();
      setServiceStatus('up');
    } catch (error) {
      console.error('Service health check failed:', error);
      setServiceStatus('down');
      // Retry after 2 seconds
      setTimeout(() => {
        checkServiceHealth();
      }, 2000);
    }
  };

  const loadIndexes = async () => {
    try {
      const indexes = await ragApi.getIndexes();
      setAvailableIndexes(Array.isArray(indexes) ? indexes : ['summarization-test', 'documents', 'knowledge-base']);
    } catch (error) {
      console.error('Failed to load indexes:', error);
      setAvailableIndexes(['summarization-test', 'documents', 'knowledge-base']);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!query.trim()) {
      setError('Please enter a search query');
      return;
    }

    setLoading(true);
    setError(null);
    setSearchResults(null);
    setSummarizationResult(null);

    try {
      if (mode === 'search') {
        await performSearch();
      } else {
        await performSummarization();
      }
    } catch (error) {
      console.error('Operation failed:', error);
      setError(error.response?.data?.message || error.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const performSearch = async () => {
    const options = {
      size: maxResults,
      minScore,
      includeEmbeddings: showVectors, // Use the state to control vector inclusion
    };

    let result;
    if (searchType === 'hybrid') {
      result = await ragApi.searchHybrid(query, selectedIndex, options);
    } else {
      result = await ragApi.searchDocuments(query, selectedIndex, options);
    }

    setSearchResults(result);
  };

  const performSummarization = async () => {
    const options = {
      maxResults,
      minScore,
      customPrompt: customPrompt.trim() || undefined,
    };

    // Use the simple summarization endpoint by default
    const result = await ragApi.summarizeQuery(query, selectedIndex, options);
    setSummarizationResult(result);
  };

  const clearResults = () => {
    setSearchResults(null);
    setSummarizationResult(null);
    setError(null);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            RAG Search Interface
          </h1>
          <p className="text-lg text-gray-600">
            Search your knowledge base or get AI-powered summaries
          </p>
          
          {/* Service Status */}
          <div className="flex items-center justify-center mt-4 space-x-2">
            {serviceStatus === 'up' ? (
              <div className="flex items-center space-x-1 text-green-600">
                <CheckCircle2 className="h-4 w-4" />
                <span className="text-sm">Service Online</span>
              </div>
            ) : serviceStatus === 'down' ? (
              <div className="flex items-center space-x-1 text-red-600">
                <AlertCircle className="h-4 w-4" />
                <span className="text-sm">Service Offline</span>
              </div>
            ) : null}
          </div>
        </div>

        {/* Main Form */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Index Selection and Mode */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Database className="inline h-4 w-4 mr-1" />
                  Knowledge Base
                </label>
                <select
                  value={selectedIndex}
                  onChange={(e) => setSelectedIndex(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  {availableIndexes.map((index) => (
                    <option key={index} value={index}>
                      {index}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Result Type
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => {setMode('search'); clearResults();}}
                    className={`flex items-center justify-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      mode === 'search'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    <List className="h-4 w-4" />
                    <span>Search</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => {setMode('summarize'); clearResults();}}
                    className={`flex items-center justify-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      mode === 'summarize'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    <Brain className="h-4 w-4" />
                    <span>Summarize</span>
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Settings className="inline h-4 w-4 mr-1" />
                  Options
                </label>
                <button
                  type="button"
                  onClick={() => setShowAdvanced(!showAdvanced)}
                  className="w-full px-3 py-2 text-sm text-gray-600 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors"
                >
                  {showAdvanced ? 'Hide' : 'Show'} Advanced Settings
                </button>
              </div>
            </div>

            {/* Advanced Settings */}
            {showAdvanced && (
              <div className="bg-gray-50 rounded-lg p-4 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Max Results
                    </label>
                    <input
                      type="number"
                      min="1"
                      max="50"
                      value={maxResults}
                      onChange={(e) => setMaxResults(parseInt(e.target.value) || 10)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Min Score
                    </label>
                    <input
                      type="number"
                      min="0"
                      max="1"
                      step="0.01"
                      value={minScore}
                      onChange={(e) => setMinScore(parseFloat(e.target.value) || 0.01)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  {mode === 'search' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Search Type
                      </label>
                      <select
                        value={searchType}
                        onChange={(e) => setSearchType(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="vector">Vector Search</option>
                        <option value="hybrid">Hybrid Search</option>
                      </select>
                    </div>
                  )}
                </div>

                {mode === 'summarize' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Custom Prompt (optional)
                    </label>
                    <textarea
                      value={customPrompt}
                      onChange={(e) => setCustomPrompt(e.target.value)}
                      placeholder="e.g., Focus on practical applications and benefits..."
                      rows={3}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                    />
                  </div>
                )}
              </div>
            )}

            {/* Search Input */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Search Query
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Enter your question in natural language..."
                  className="w-full px-4 py-3 pr-12 text-lg border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  disabled={loading}
                />
                <button
                  type="submit"
                  disabled={loading || !query.trim()}
                  className="absolute right-2 top-1/2 transform -translate-y-1/2 p-2 text-blue-600 hover:text-blue-700 disabled:text-gray-400 transition-colors"
                >
                  {mode === 'summarize' ? (
                    <Sparkles className="h-5 w-5" />
                  ) : (
                    <Search className="h-5 w-5" />
                  )}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading || !query.trim()}
              className={`w-full flex items-center justify-center space-x-2 px-6 py-3 rounded-lg text-white font-medium transition-all ${
                loading || !query.trim()
                  ? 'bg-gray-400 cursor-not-allowed'
                  : mode === 'summarize'
                  ? 'bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 shadow-lg hover:shadow-xl'
                  : 'bg-gradient-to-r from-blue-600 to-green-600 hover:from-blue-700 hover:to-green-700 shadow-lg hover:shadow-xl'
              }`}
            >
              {loading ? (
                <>
                  <div className="loading-dots scale-50">
                    <div></div><div></div><div></div><div></div>
                  </div>
                  <span>Processing...</span>
                </>
              ) : mode === 'summarize' ? (
                <>
                  <Sparkles className="h-5 w-5" />
                  <span>Generate AI Summary</span>
                </>
              ) : (
                <>
                  <Search className="h-5 w-5" />
                  <span>Search Documents</span>
                </>
              )}
            </button>
          </form>
        </div>

        {/* Results */}
        {mode === 'search' && (
          <SearchResults 
            results={searchResults} 
            loading={loading} 
            error={error}
            showVectors={showVectors}
            onShowVectorsChange={setShowVectors}
          />
        )}

        {mode === 'summarize' && (
          <Summarization 
            result={summarizationResult} 
            loading={loading} 
            error={error} 
          />
        )}
      </div>
    </div>
  );
};

export default RagInterface;
