import React from 'react';
import { Clock, FileText, Zap, Brain, Lightbulb, ExternalLink } from 'lucide-react';

const Summarization = ({ result, loading, error }) => {
  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-8 text-center">
        <div className="loading-dots mx-auto">
          <div></div>
          <div></div>
          <div></div>
          <div></div>
        </div>
        <p className="text-gray-500 mt-4">Generating summary...</p>
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
            <h3 className="text-sm font-medium text-red-800">Summarization Error</h3>
            <p className="text-sm text-red-700 mt-1">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!result || !result.summary) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-8 text-center">
        <Brain className="mx-auto h-12 w-12 text-gray-400" />
        <h3 className="mt-2 text-sm font-medium text-gray-900">No summary available</h3>
        <p className="mt-1 text-sm text-gray-500">
          Unable to generate a summary. Try a different query or check if the index contains relevant data.
        </p>
      </div>
    );
  }

  const formatTime = (timeMs) => {
    if (timeMs < 1000) return `${timeMs}ms`;
    return `${(timeMs / 1000).toFixed(1)}s`;
  };

  return (
    <div className="space-y-6">
      {/* Summary Header */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center space-x-2 mb-2">
              <Lightbulb className="h-5 w-5 text-yellow-500" />
              <h2 className="text-lg font-semibold text-gray-900">AI Summary</h2>
            </div>
            <p className="text-sm text-gray-500">
              Generated from {result.totalResults || 0} relevant documents
            </p>
          </div>
          
          {/* Metrics */}
          <div className="flex items-center space-x-6 text-sm text-gray-500">
            {result.processingTimeMs && (
              <div className="flex items-center space-x-1">
                <Clock className="h-4 w-4" />
                <span>{formatTime(result.processingTimeMs)}</span>
              </div>
            )}
            
            {result.model && (
              <div className="flex items-center space-x-1">
                <Brain className="h-4 w-4" />
                <span>{result.model}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Main Summary */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="prose max-w-none">
          <div className="text-gray-800 leading-relaxed text-base">
            {result.summary.split('\\n\\n').map((paragraph, index) => (
              <p key={index} className="mb-4 last:mb-0">
                {paragraph}
              </p>
            ))}
          </div>
        </div>
      </div>

      {/* Source References */}
      {result.sourceReferences && result.sourceReferences.length > 0 && (
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center space-x-2 mb-4">
            <FileText className="h-5 w-5 text-blue-500" />
            <h3 className="text-lg font-medium text-gray-900">Source References</h3>
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {result.sourceReferences.map((source, index) => (
              <div
                key={index}
                className="p-3 border border-gray-200 rounded-lg hover:border-blue-300 transition-colors"
              >
                <div className="flex items-start space-x-2">
                  <ExternalLink className="h-4 w-4 text-gray-400 mt-0.5 flex-shrink-0" />
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {source.title || source.name || `Reference ${index + 1}`}
                    </p>
                    {source.type && (
                      <p className="text-xs text-gray-500 mt-1">
                        {source.type}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Search Results (if included) */}
      {result.searchResults && result.searchResults.length > 0 && (
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center space-x-2 mb-4">
            <FileText className="h-5 w-5 text-green-500" />
            <h3 className="text-lg font-medium text-gray-900">Supporting Documents</h3>
            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
              {result.searchResults.length} documents
            </span>
          </div>
          
          <div className="space-y-4">
            {result.searchResults.slice(0, 5).map((searchResult, index) => (
              <div
                key={index}
                className="p-4 border border-gray-200 rounded-lg"
              >
                <div className="flex items-start justify-between mb-2">
                  <div className="flex-1">
                    {searchResult.document?.metadata?.title && (
                      <h4 className="text-sm font-medium text-gray-900 mb-1">
                        {searchResult.document.metadata.title}
                      </h4>
                    )}
                    <div className="flex items-center space-x-3 text-xs text-gray-500">
                      <span className="font-mono">
                        {(searchResult.score * 100).toFixed(1)}% match
                      </span>
                      {searchResult.document?.source && (
                        <span>{searchResult.document.source}</span>
                      )}
                      {searchResult.document?.metadata?.category && (
                        <span className="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                          {searchResult.document.metadata.category}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                
                <p className="text-sm text-gray-600 line-clamp-3">
                  {searchResult.document?.content?.substring(0, 200)}
                  {searchResult.document?.content && searchResult.document.content.length > 200 && '...'}
                </p>
              </div>
            ))}
            
            {result.searchResults.length > 5 && (
              <div className="text-center">
                <p className="text-sm text-gray-500">
                  And {result.searchResults.length - 5} more supporting documents...
                </p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Performance Metrics */}
      {(result.searchTimeMs || result.summarizationTimeMs) && (
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="flex items-center space-x-2 mb-3">
            <Zap className="h-4 w-4 text-gray-500" />
            <h4 className="text-sm font-medium text-gray-700">Performance Metrics</h4>
          </div>
          
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
            {result.searchTimeMs && (
              <div className="text-center">
                <div className="text-lg font-semibold text-blue-600">
                  {formatTime(result.searchTimeMs)}
                </div>
                <div className="text-gray-500">Search Time</div>
              </div>
            )}
            
            {result.summarizationTimeMs && (
              <div className="text-center">
                <div className="text-lg font-semibold text-green-600">
                  {formatTime(result.summarizationTimeMs)}
                </div>
                <div className="text-gray-500">Summary Time</div>
              </div>
            )}
            
            {result.totalProcessingTimeMs && (
              <div className="text-center">
                <div className="text-lg font-semibold text-purple-600">
                  {formatTime(result.totalProcessingTimeMs)}
                </div>
                <div className="text-gray-500">Total Time</div>
              </div>
            )}
            
            {result.totalResults && (
              <div className="text-center">
                <div className="text-lg font-semibold text-gray-600">
                  {result.totalResults}
                </div>
                <div className="text-gray-500">Documents</div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Summarization;
