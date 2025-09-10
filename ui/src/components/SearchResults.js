import React, { useState } from 'react';
import { ChevronDown, ChevronUp, Eye, EyeOff, FileText, Clock, User, ExternalLink, X } from 'lucide-react';

const SearchResults = ({ results, loading, error, showVectors, onShowVectorsChange }) => {
  const [expandedResults, setExpandedResults] = useState(new Set());
  const [fileViewerModal, setFileViewerModal] = useState(null);
  const [fileContent, setFileContent] = useState(null);
  const [loadingFile, setLoadingFile] = useState(false);

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

  const handleFilePathClick = async (filePath) => {
    try {
      setLoadingFile(true);
      setFileViewerModal({ path: filePath });
      
      // Encode the file path in base64 for security
      const encodedPath = btoa(filePath);
      
      const response = await fetch(`http://localhost:8080/api/v1/file-viewer/content?path=${encodedPath}`);
      
      if (!response.ok) {
        throw new Error(`Failed to load file: ${response.statusText}`);
      }
      
      const data = await response.json();
      setFileContent(data);
    } catch (error) {
      console.error('Error loading file:', error);
      setFileContent({ error: error.message });
    } finally {
      setLoadingFile(false);
    }
  };

  const closeFileViewer = () => {
    setFileViewerModal(null);
    setFileContent(null);
  };

  const renderMetadataValue = (key, value) => {
    // Check if this looks like a file path
    if (key === 'path' || (typeof value === 'string' && value.match(/^(\/|[A-Za-z]:\\).+\.(txt|pdf|doc|docx|html|htm|xml|rtf|odt|ods|odp|pptx|ppt|xlsx|xls|csv)$/i))) {
      return (
        <button
          onClick={() => handleFilePathClick(value)}
          className="text-blue-600 hover:text-blue-800 hover:underline inline-flex items-center space-x-1"
          title="Click to view file content"
        >
          <span>{String(value)}</span>
          <ExternalLink className="h-3 w-3" />
        </button>
      );
    }
    
    return <span className="text-blue-700 ml-1">{String(value)}</span>;
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
                          {renderMetadataValue(key, value)}
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
      
      {/* File Viewer Modal */}
      {fileViewerModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] flex flex-col">
            {/* Modal Header */}
            <div className="flex items-center justify-between p-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">File Viewer</h3>
              <button
                onClick={closeFileViewer}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="h-6 w-6" />
              </button>
            </div>
            
            {/* Modal Content */}
            <div className="flex-1 overflow-hidden">
              {loadingFile ? (
                <div className="flex items-center justify-center h-64">
                  <div className="loading-dots">
                    <div></div>
                    <div></div>
                    <div></div>
                    <div></div>
                  </div>
                  <p className="text-gray-500 ml-4">Loading file...</p>
                </div>
              ) : fileContent?.error ? (
                <div className="p-4 text-center text-red-600">
                  <p>Error: {fileContent.error}</p>
                </div>
              ) : fileContent ? (
                <div className="p-4 h-full flex flex-col">
                  {/* File Info */}
                  <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div><span className="font-medium">File:</span> {fileContent.fileName}</div>
                      <div><span className="font-medium">Size:</span> {fileContent.fileSize} bytes</div>
                      <div><span className="font-medium">Type:</span> {fileContent.mimeType}</div>
                      <div><span className="font-medium">Path:</span> {fileContent.filePath}</div>
                    </div>
                  </div>
                  
                  {/* File Content */}
                  <div className="flex-1 overflow-auto border border-gray-200 rounded-lg">
                    <pre className="p-4 text-sm text-gray-800 whitespace-pre-wrap font-mono">
                      {fileContent.content}
                    </pre>
                  </div>
                </div>
              ) : null}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchResults;
