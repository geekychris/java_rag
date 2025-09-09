import React, { useState } from 'react';
import { ChevronDown, ChevronUp, Eye, EyeOff, FileText, Clock, User } from 'lucide-react';

const SearchResults = ({ results, loading, error, showVectors, onShowVectorsChange }) => {
  const [expandedResults, setExpandedResults] = useState(new Set());

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-8 text-center">
        <div className="loading-dots mx-auto">
          <div></div>
          <div></div>
          <div></div>
          <div></div>
        </div>
        <p className="text-gray-500 mt-4">Searching documents...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6 border-l-4 border-red-500">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <div className="w-5 h-5 text-red-400">⚠</div>
          </div>
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Search Error</h3>
            <p className="text-sm text-red-700 mt-1">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!results || !results.results || results.results.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-8 text-center">
        <FileText className="mx-auto h-12 w-12 text-gray-400" />
        <h3 className="mt-2 text-sm font-medium text-gray-900">No results found</h3>
        <p className="mt-1 text-sm text-gray-500">
          Try adjusting your search query or check if the selected index contains data.
        </p>
      </div>
    );
  }

  const toggleExpanded = (index) => {
    const newExpanded = new Set(expandedResults);
    if (newExpanded.has(index)) {
      newExpanded.delete(index);
    } else {
      newExpanded.add(index);
    }
    setExpandedResults(newExpanded);
  };

  const formatScore = (score) => {
    return (score * 100).toFixed(1);
  };

  const truncateText = (text, maxLength = 300) => {
    if (!text) return '';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  };

  return (
    <div className="space-y-4">
      {/* Header with controls */}
      <div className="bg-white rounded-lg shadow-sm p-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Search Results</h2>
            <p className="text-sm text-gray-500 mt-1">
              Found {results.totalResults} documents matching your query
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <button
              onClick={() => onShowVectorsChange && onShowVectorsChange(!showVectors)}
              className={`flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                showVectors
                  ? 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {showVectors ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              <span>{showVectors ? 'Hide' : 'Show'} Vectors</span>
            </button>
          </div>
        </div>
      </div>

      {/* Results list */}
      <div className="space-y-4">
        {results.results.map((result, index) => {
          const isExpanded = expandedResults.has(index);
          const document = result.document;
          
          return (
            <div
              key={index}
              className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
            >
              {/* Result header */}
              <div className="p-4 border-b border-gray-100">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3">
                      <div className="flex-shrink-0">
                        <div className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          result.score > 0.8 
                            ? 'bg-green-100 text-green-800'
                            : result.score > 0.5
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {formatScore(result.score)}% match
                        </div>
                      </div>
                      {document.metadata?.title && (
                        <h3 className="text-lg font-medium text-gray-900">
                          {document.metadata.title}
                        </h3>
                      )}
                    </div>
                    
                    {/* Metadata */}
                    <div className="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                      {document.metadata?.author && (
                        <div className="flex items-center space-x-1">
                          <User className="h-4 w-4" />
                          <span>{document.metadata.author}</span>
                        </div>
                      )}
                      {document.metadata?.category && (
                        <div className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          {document.metadata.category}
                        </div>
                      )}
                      {document.source && (
                        <div className="flex items-center space-x-1">
                          <FileText className="h-4 w-4" />
                          <span>{document.source}</span>
                        </div>
                      )}
                      {document.timestamp && (
                        <div className="flex items-center space-x-1">
                          <Clock className="h-4 w-4" />
                          <span>{new Date(document.timestamp).toLocaleDateString()}</span>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  <button
                    onClick={() => toggleExpanded(index)}
                    className="flex-shrink-0 p-1 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {isExpanded ? (
                      <ChevronUp className="h-5 w-5" />
                    ) : (
                      <ChevronDown className="h-5 w-5" />
                    )}
                  </button>
                </div>
              </div>

              {/* Content */}
              <div className="p-4">
                <div className="prose max-w-none">
                  <p className="text-gray-700 leading-relaxed">
                    {isExpanded ? document.content : truncateText(document.content)}
                  </p>
                </div>

                {/* Vector embeddings (if enabled) */}
                {showVectors && document.embedding && (
                  <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                    <div className="flex items-center justify-between mb-2">
                      <h4 className="text-sm font-medium text-gray-700">Vector Embedding</h4>
                      <span className="text-xs text-gray-500">
                        {document.embedding.length} dimensions
                      </span>
                    </div>
                    <div className="max-h-32 overflow-y-auto custom-scrollbar">
                      <div className="text-xs text-gray-600 font-mono break-all">
                        [{document.embedding.slice(0, 10).map(val => val.toFixed(4)).join(', ')}
                        {document.embedding.length > 10 && ', ...'}]
                      </div>
                    </div>
                  </div>
                )}

                {/* Additional metadata */}
                {document.metadata && Object.keys(document.metadata).length > 0 && (
                  <div className="mt-4 p-3 bg-blue-50 rounded-lg">
                    <h4 className="text-sm font-medium text-blue-900 mb-2">Additional Metadata</h4>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      {Object.entries(document.metadata).map(([key, value]) => (
                        <div key={key} className="text-sm">
                          <span className="font-medium text-blue-800">{key}:</span>
                          <span className="text-blue-700 ml-1">{String(value)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default SearchResults;
