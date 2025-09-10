import React, { useState } from 'react';
import { Database, Upload, Plus, FileText, AlertCircle, CheckCircle2, X } from 'lucide-react';
import ragApi from '../services/ragApi';

const IndexManager = ({ onIndexCreated, availableIndexes }) => {
  const [showCreateIndex, setShowCreateIndex] = useState(false);
  const [showUploadCsv, setShowUploadCsv] = useState(false);
  const [newIndexName, setNewIndexName] = useState('');
  const [selectedUploadIndex, setSelectedUploadIndex] = useState('');
  const [csvFile, setCsvFile] = useState(null);
  const [csvContent, setCsvContent] = useState('');
  const [contentColumnName, setContentColumnName] = useState('content');
  const [docIdColumnName, setDocIdColumnName] = useState('');
  const [csvHeaders, setCsvHeaders] = useState([]);
  const [csvPreview, setCsvPreview] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleCreateIndex = async (e) => {
    e.preventDefault();
    
    if (!newIndexName.trim()) {
      setError('Please enter an index name');
      return;
    }

    // Validate index name (lowercase, alphanumeric with hyphens/underscores)
    const validName = /^[a-z0-9_-]+$/.test(newIndexName);
    if (!validName) {
      setError('Index name must be lowercase alphanumeric with hyphens or underscores only');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const result = await ragApi.createIndex(newIndexName);
      setSuccess(`Index "${newIndexName}" created successfully!`);
      setNewIndexName('');
      setShowCreateIndex(false);
      
      // Notify parent component to refresh index list
      if (onIndexCreated) {
        onIndexCreated(newIndexName);
      }
    } catch (error) {
      console.error('Failed to create index:', error);
      setError(error.response?.data?.message || error.message || 'Failed to create index');
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.type !== 'text/csv' && !file.name.endsWith('.csv')) {
        setError('Please select a CSV file');
        return;
      }
      
      setCsvFile(file);
      setError(null);
      
      // Read file content
      const reader = new FileReader();
      reader.onload = (event) => {
        const content = event.target.result;
        setCsvContent(content);
        
        // Parse CSV to extract headers and preview
        try {
          const lines = content.split('\n').filter(line => line.trim());
          if (lines.length > 0) {
            const headers = lines[0].split(',').map(header => header.replace(/"/g, '').trim());
            setCsvHeaders(headers);
            
            // Set default content column if it exists
            const commonContentColumns = ['content', 'text', 'description', 'body', 'question'];
            const defaultContentCol = headers.find(h => 
              commonContentColumns.some(col => h.toLowerCase().includes(col.toLowerCase()))
            ) || headers[headers.length - 1]; // fallback to last column
            if (defaultContentCol) {
              setContentColumnName(defaultContentCol);
            }
            
            // Set default doc ID column if it exists
            const commonIdColumns = ['id', 'doc_id', 'document_id', 'paper_id', 'article_id', 'uuid'];
            const defaultIdCol = headers.find(h => 
              commonIdColumns.some(col => h.toLowerCase().includes(col.toLowerCase()))
            );
            if (defaultIdCol) {
              setDocIdColumnName(defaultIdCol);
            }
            
            // Create preview data (first 3 rows)
            const previewData = lines.slice(1, 4).map(line => {
              const values = line.split(',').map(val => val.replace(/"/g, '').trim());
              const row = {};
              headers.forEach((header, index) => {
                row[header] = values[index] || '';
              });
              return row;
            });
            setCsvPreview(previewData);
          }
        } catch (error) {
          console.error('Error parsing CSV:', error);
        }
      };
      reader.readAsText(file);
    }
  };

  const handleUploadCsv = async (e) => {
    e.preventDefault();
    
    if (!selectedUploadIndex) {
      setError('Please select an index');
      return;
    }
    
    if (!csvContent.trim()) {
      setError('Please select a CSV file');
      return;
    }
    
    if (!contentColumnName.trim()) {
      setError('Please specify the content column name');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const uploadOptions = {
        contentColumnName,
        source: 'ui-upload'
      };
      
      // Include docIdColumnName if specified
      if (docIdColumnName && docIdColumnName.trim()) {
        uploadOptions.docIdColumnName = docIdColumnName;
      }
      
      const result = await ragApi.uploadCsv(selectedUploadIndex, csvContent, uploadOptions);
      
      setSuccess(`Successfully uploaded ${result.documentsIngested || result.documentsIndexed || 'documents'} from ${csvFile.name} to index "${selectedUploadIndex}"`);
      
      // Reset all form state
      setCsvFile(null);
      setCsvContent('');
      setCsvHeaders([]);
      setCsvPreview([]);
      setContentColumnName('content');
      setDocIdColumnName('');
      setSelectedUploadIndex('');
      setShowUploadCsv(false);
      
      // Reset file input
      const fileInput = document.getElementById('csv-file-input');
      if (fileInput) {
        fileInput.value = '';
      }
    } catch (error) {
      console.error('Failed to upload CSV:', error);
      setError(error.response?.data?.message || error.message || 'Failed to upload CSV file');
    } finally {
      setLoading(false);
    }
  };

  const clearMessages = () => {
    setError(null);
    setSuccess(null);
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-semibold text-gray-900 flex items-center space-x-2">
          <Database className="h-5 w-5" />
          <span>Index & Document Management</span>
        </h2>
      </div>

      {/* Action Buttons */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <button
          onClick={() => {
            setShowCreateIndex(!showCreateIndex);
            setShowUploadCsv(false);
            clearMessages();
          }}
          className="flex items-center justify-center space-x-2 px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="h-4 w-4" />
          <span>Create New Index</span>
        </button>
        
        <button
          onClick={() => {
            setShowUploadCsv(!showUploadCsv);
            setShowCreateIndex(false);
            clearMessages();
          }}
          className="flex items-center justify-center space-x-2 px-4 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
        >
          <Upload className="h-4 w-4" />
          <span>Upload CSV Documents</span>
        </button>
      </div>

      {/* Messages */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-start space-x-2">
          <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-red-800 text-sm">{error}</p>
          </div>
          <button onClick={clearMessages} className="text-red-400 hover:text-red-600">
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {success && (
        <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg flex items-start space-x-2">
          <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-green-800 text-sm">{success}</p>
          </div>
          <button onClick={clearMessages} className="text-green-400 hover:text-green-600">
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Create Index Form */}
      {showCreateIndex && (
        <div className="bg-gray-50 rounded-lg p-4 mb-4">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Create New Index</h3>
          <form onSubmit={handleCreateIndex} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Index Name
              </label>
              <input
                type="text"
                value={newIndexName}
                onChange={(e) => setNewIndexName(e.target.value.toLowerCase())}
                placeholder="e.g., my-documents, knowledge-base, customer-data"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                disabled={loading}
                pattern="[a-z0-9_-]+"
                title="Only lowercase letters, numbers, hyphens, and underscores allowed"
              />
              <p className="mt-1 text-xs text-gray-500">
                Use lowercase letters, numbers, hyphens, and underscores only
              </p>
            </div>
            
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setShowCreateIndex(false)}
                className="px-4 py-2 text-sm text-gray-600 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                disabled={loading}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading || !newIndexName.trim()}
                className="px-4 py-2 text-sm text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
              >
                {loading ? 'Creating...' : 'Create Index'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Upload CSV Form */}
      {showUploadCsv && (
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Upload CSV Documents</h3>
          <form onSubmit={handleUploadCsv} className="space-y-4">
            <div className="grid grid-cols-1 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Target Index
                </label>
                <select
                  value={selectedUploadIndex}
                  onChange={(e) => setSelectedUploadIndex(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  disabled={loading}
                >
                  <option value="">Select an index...</option>
                  {availableIndexes.map((index) => (
                    <option key={index} value={index}>
                      {index}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            
            {csvHeaders.length > 0 && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Content Column *
                  </label>
                  <select
                    value={contentColumnName}
                    onChange={(e) => setContentColumnName(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    disabled={loading}
                  >
                    {csvHeaders.map((header) => (
                      <option key={header} value={header}>
                        {header}
                      </option>
                    ))}
                  </select>
                  <p className="mt-1 text-xs text-gray-500">
                    Column containing the main document text
                  </p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Document ID Column (optional)
                  </label>
                  <select
                    value={docIdColumnName}
                    onChange={(e) => setDocIdColumnName(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    disabled={loading}
                  >
                    <option value="">Generate automatic IDs</option>
                    {csvHeaders.map((header) => (
                      <option key={header} value={header}>
                        {header}
                      </option>
                    ))}
                  </select>
                  <p className="mt-1 text-xs text-gray-500">
                    Column containing custom document IDs
                  </p>
                </div>
              </div>
            )}
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                CSV File
              </label>
              <div className="flex items-center justify-center w-full">
                <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100 transition-colors">
                  <div className="flex flex-col items-center justify-center pt-5 pb-6">
                    {csvFile ? (
                      <>
                        <FileText className="w-8 h-8 text-green-600 mb-2" />
                        <p className="text-sm text-gray-700 font-medium">{csvFile.name}</p>
                        <p className="text-xs text-gray-500">Click to change file</p>
                      </>
                    ) : (
                      <>
                        <Upload className="w-8 h-8 text-gray-400 mb-2" />
                        <p className="text-sm text-gray-500">Click to upload CSV file</p>
                        <p className="text-xs text-gray-400">CSV files only</p>
                      </>
                    )}
                  </div>
                  <input
                    id="csv-file-input"
                    type="file"
                    accept=".csv,text/csv"
                    onChange={handleFileChange}
                    className="hidden"
                    disabled={loading}
                  />
                </label>
              </div>
            </div>
            
            {csvFile && csvHeaders.length > 0 && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h4 className="text-sm font-medium text-blue-900 mb-3">CSV File Preview</h4>
                
                <div className="space-y-3">
                  <div className="text-sm text-blue-800">
                    <strong>File:</strong> {csvFile.name} ({csvHeaders.length} columns)
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                    <div>
                      <strong className="text-blue-900">Content Column:</strong>
                      <div className="bg-blue-100 px-2 py-1 rounded mt-1 text-blue-800">
                        {contentColumnName} → Main searchable content
                      </div>
                    </div>
                    
                    <div>
                      <strong className="text-blue-900">Document ID Column:</strong>
                      <div className="bg-blue-100 px-2 py-1 rounded mt-1 text-blue-800">
                        {docIdColumnName ? `${docIdColumnName} → Custom document IDs` : 'Auto-generated UUIDs'}
                      </div>
                    </div>
                  </div>
                  
                  <div>
                    <strong className="text-blue-900 text-sm">All Columns to be Indexed:</strong>
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2 mt-2">
                      {csvHeaders.map((header) => {
                        let usage = 'metadata';
                        if (header === contentColumnName) usage = 'content';
                        if (header === docIdColumnName) usage = 'doc_id';
                        
                        const bgColor = usage === 'content' ? 'bg-green-100 text-green-800' :
                                       usage === 'doc_id' ? 'bg-purple-100 text-purple-800' :
                                       'bg-gray-100 text-gray-700';
                        
                        return (
                          <div key={header} className={`px-2 py-1 rounded text-xs ${bgColor}`}>
                            <div className="font-medium">{header}</div>
                            <div className="text-xs opacity-75">{usage}</div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                  
                  {csvPreview.length > 0 && (
                    <div>
                      <strong className="text-blue-900 text-sm">Sample Data (first {csvPreview.length} rows):</strong>
                      <div className="mt-2 max-h-32 overflow-y-auto">
                        <table className="min-w-full text-xs">
                          <thead>
                            <tr className="bg-blue-100">
                              {csvHeaders.slice(0, 4).map((header) => (
                                <th key={header} className="px-2 py-1 text-left text-blue-900 font-medium">
                                  {header}
                                </th>
                              ))}
                              {csvHeaders.length > 4 && (
                                <th className="px-2 py-1 text-left text-blue-900 font-medium">...</th>
                              )}
                            </tr>
                          </thead>
                          <tbody>
                            {csvPreview.map((row, index) => (
                              <tr key={index} className="border-b border-blue-200">
                                {csvHeaders.slice(0, 4).map((header) => (
                                  <td key={header} className="px-2 py-1 text-blue-800">
                                    {String(row[header] || '').length > 30 
                                      ? String(row[header]).substring(0, 30) + '...' 
                                      : row[header] || ''}
                                  </td>
                                ))}
                                {csvHeaders.length > 4 && (
                                  <td className="px-2 py-1 text-blue-600">...</td>
                                )}
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
            
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => {
                  setShowUploadCsv(false);
                  setCsvFile(null);
                  setCsvContent('');
                }}
                className="px-4 py-2 text-sm text-gray-600 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                disabled={loading}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading || !selectedUploadIndex || !csvContent.trim()}
                className="px-4 py-2 text-sm text-white bg-green-600 rounded-md hover:bg-green-700 disabled:bg-gray-400 transition-colors"
              >
                {loading ? 'Uploading...' : 'Upload CSV'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default IndexManager;
