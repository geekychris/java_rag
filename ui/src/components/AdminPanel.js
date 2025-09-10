import React, { useState, useEffect } from 'react';
import { 
  Settings, 
  Plus, 
  Upload, 
  Trash2, 
  Database, 
  AlertTriangle,
  CheckCircle,
  RefreshCw,
  X,
  Eye,
  EyeOff
} from 'lucide-react';
import ragApi from '../services/ragApi';

const AdminPanel = ({ isVisible, onToggle }) => {
  const [activeTab, setActiveTab] = useState('indexes');
  const [indexes, setIndexes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Index management state
  const [newIndexName, setNewIndexName] = useState('');
  const [deleteConfirmIndex, setDeleteConfirmIndex] = useState(null);

  // CSV upload state
  const [selectedIndex, setSelectedIndex] = useState('');
  const [csvFile, setCsvFile] = useState(null);
  const [csvContent, setCsvContent] = useState('');
  const [contentColumn, setContentColumn] = useState('content');
  const [sourceTag, setSourceTag] = useState('csv-upload');
  const [csvHeaders, setCsvHeaders] = useState([]);
  const [showCsvPreview, setShowCsvPreview] = useState(false);

  useEffect(() => {
    if (isVisible) {
      loadIndexes();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isVisible]);

  const clearMessages = () => {
    setError(null);
    setSuccess(null);
  };

  const showSuccess = (message) => {
    clearMessages();
    setSuccess(message);
    setTimeout(() => setSuccess(null), 5000);
  };

  const showError = (message) => {
    clearMessages();
    setError(message);
    setTimeout(() => setError(null), 8000);
  };

  const loadIndexes = async () => {
    setLoading(true);
    try {
      const indexList = await ragApi.getIndexes();
      setIndexes(Array.isArray(indexList) ? indexList : []);
      if (!selectedIndex && indexList.length > 0) {
        setSelectedIndex(indexList[0]);
      }
    } catch (error) {
      showError(`Failed to load indexes: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const createIndex = async (e) => {
    e.preventDefault();
    if (!newIndexName.trim()) {
      showError('Please enter an index name');
      return;
    }

    if (!/^[a-z0-9_-]+$/.test(newIndexName)) {
      showError('Index name can only contain lowercase letters, numbers, hyphens, and underscores');
      return;
    }

    setLoading(true);
    try {
      await ragApi.createIndex(newIndexName.trim());
      showSuccess(`Index '${newIndexName}' created successfully`);
      setNewIndexName('');
      await loadIndexes();
    } catch (error) {
      showError(`Failed to create index: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const deleteIndex = async (indexName) => {
    setLoading(true);
    try {
      await ragApi.deleteIndex(indexName);
      showSuccess(`Index '${indexName}' deleted successfully`);
      setDeleteConfirmIndex(null);
      await loadIndexes();
      if (selectedIndex === indexName) {
        setSelectedIndex(indexes.length > 1 ? indexes.find(idx => idx !== indexName) : '');
      }
    } catch (error) {
      showError(`Failed to delete index: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      setCsvFile(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        const content = e.target.result;
        setCsvContent(content);
        
        // Parse headers from first line
        const lines = content.split('\n');
        if (lines.length > 0) {
          const headers = lines[0].split(',').map(h => h.trim().replace(/"/g, ''));
          setCsvHeaders(headers);
          
          // Auto-select content column if it exists
          const contentCols = headers.filter(h => 
            h.toLowerCase().includes('content') || 
            h.toLowerCase().includes('text') ||
            h.toLowerCase().includes('description')
          );
          if (contentCols.length > 0) {
            setContentColumn(contentCols[0]);
          }
        }
      };
      reader.readAsText(file);
    }
  };

  const uploadCsv = async (e) => {
    e.preventDefault();
    if (!selectedIndex) {
      showError('Please select an index');
      return;
    }
    if (!csvContent) {
      showError('Please select a CSV file');
      return;
    }
    if (!contentColumn) {
      showError('Please specify the content column name');
      return;
    }

    setLoading(true);
    try {
      const result = await ragApi.uploadCsv(selectedIndex, csvContent, {
        contentColumnName: contentColumn,
        source: sourceTag || 'csv-upload'
      });
      
      showSuccess(`Successfully uploaded ${result.documentsIngested} documents to '${selectedIndex}'`);
      setCsvFile(null);
      setCsvContent('');
      setCsvHeaders([]);
      setShowCsvPreview(false);
      
      // Reset file input
      const fileInput = document.getElementById('csv-file-input');
      if (fileInput) fileInput.value = '';
      
    } catch (error) {
      showError(`Failed to upload CSV: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const getCsvPreview = () => {
    if (!csvContent) return '';
    const lines = csvContent.split('\n').slice(0, 6); // First 5 rows + header
    return lines.join('\n');
  };

  if (!isVisible) {
    return (
      <button
        onClick={onToggle}
        className="fixed bottom-4 right-4 p-3 bg-blue-600 text-white rounded-full shadow-lg hover:bg-blue-700 transition-colors z-10"
        title="Open Admin Panel"
      >
        <Settings className="h-5 w-5" />
      </button>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
      <div className="bg-white rounded-lg shadow-xl max-w-4xl max-h-[90vh] w-full mx-4 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 bg-gray-50">
          <div className="flex items-center space-x-2">
            <Settings className="h-6 w-6 text-gray-700" />
            <h2 className="text-xl font-semibold text-gray-800">Admin Panel</h2>
          </div>
          <button
            onClick={onToggle}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Messages */}
        {(success || error) && (
          <div className="p-4 border-b border-gray-200">
            {success && (
              <div className="flex items-center space-x-2 text-green-700 bg-green-50 p-3 rounded-lg">
                <CheckCircle className="h-5 w-5" />
                <span>{success}</span>
              </div>
            )}
            {error && (
              <div className="flex items-center space-x-2 text-red-700 bg-red-50 p-3 rounded-lg">
                <AlertTriangle className="h-5 w-5" />
                <span>{error}</span>
              </div>
            )}
          </div>
        )}

        {/* Tabs */}
        <div className="flex border-b border-gray-200">
          <button
            onClick={() => setActiveTab('indexes')}
            className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'indexes'
                ? 'border-blue-500 text-blue-600 bg-blue-50'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
            }`}
          >
            <Database className="inline h-4 w-4 mr-2" />
            Index Management
          </button>
          <button
            onClick={() => setActiveTab('upload')}
            className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'upload'
                ? 'border-blue-500 text-blue-600 bg-blue-50'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
            }`}
          >
            <Upload className="inline h-4 w-4 mr-2" />
            CSV Upload
          </button>
        </div>

        {/* Content */}
        <div className="p-6 max-h-[60vh] overflow-y-auto">
          {activeTab === 'indexes' && (
            <div className="space-y-6">
              {/* Create Index */}
              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="text-lg font-medium text-gray-800 mb-4">Create New Index</h3>
                <form onSubmit={createIndex} className="flex space-x-4">
                  <input
                    type="text"
                    value={newIndexName}
                    onChange={(e) => setNewIndexName(e.target.value)}
                    placeholder="Enter index name (e.g., my-documents)"
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    disabled={loading}
                  />
                  <button
                    type="submit"
                    disabled={loading || !newIndexName.trim()}
                    className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
                  >
                    <Plus className="h-4 w-4" />
                    <span>Create</span>
                  </button>
                </form>
                <p className="text-sm text-gray-500 mt-2">
                  Index names must contain only lowercase letters, numbers, hyphens, and underscores.
                </p>
              </div>

              {/* Index List */}
              <div>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-medium text-gray-800">Existing Indexes</h3>
                  <button
                    onClick={loadIndexes}
                    disabled={loading}
                    className="flex items-center space-x-2 px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-md transition-colors"
                  >
                    <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                    <span>Refresh</span>
                  </button>
                </div>

                {loading && indexes.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">Loading indexes...</div>
                ) : indexes.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">No indexes found</div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {indexes.map((indexName) => (
                      <div key={indexName} className="bg-white border border-gray-200 rounded-lg p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-2">
                            <Database className="h-5 w-5 text-blue-500" />
                            <span className="font-medium text-gray-800">{indexName}</span>
                          </div>
                          <button
                            onClick={() => setDeleteConfirmIndex(indexName)}
                            className="p-1 text-red-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors"
                            title="Delete index"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                        
                        {deleteConfirmIndex === indexName && (
                          <div className="mt-3 p-3 bg-red-50 rounded border border-red-200">
                            <p className="text-sm text-red-700 mb-2">
                              Are you sure? This will permanently delete all documents in this index.
                            </p>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => deleteIndex(indexName)}
                                disabled={loading}
                                className="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700 disabled:bg-gray-400"
                              >
                                Delete
                              </button>
                              <button
                                onClick={() => setDeleteConfirmIndex(null)}
                                className="px-3 py-1 bg-gray-300 text-gray-700 text-sm rounded hover:bg-gray-400"
                              >
                                Cancel
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'upload' && (
            <div className="space-y-6">
              <form onSubmit={uploadCsv} className="space-y-6">
                {/* Index Selection */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Target Index
                  </label>
                  <select
                    value={selectedIndex}
                    onChange={(e) => setSelectedIndex(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    disabled={loading}
                  >
                    <option value="">Select an index</option>
                    {indexes.map((indexName) => (
                      <option key={indexName} value={indexName}>
                        {indexName}
                      </option>
                    ))}
                  </select>
                  {indexes.length === 0 && (
                    <p className="text-sm text-yellow-600 mt-1">
                      No indexes available. Create one in the Index Management tab first.
                    </p>
                  )}
                </div>

                {/* File Upload */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    CSV File
                  </label>
                  <input
                    id="csv-file-input"
                    type="file"
                    accept=".csv"
                    onChange={handleFileUpload}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    disabled={loading}
                  />
                  {csvFile && (
                    <div className="mt-2 text-sm text-gray-600">
                      Selected: {csvFile.name} ({Math.round(csvFile.size / 1024)} KB)
                    </div>
                  )}
                </div>

                {/* Configuration */}
                {csvHeaders.length > 0 && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Content Column
                      </label>
                      <select
                        value={contentColumn}
                        onChange={(e) => setContentColumn(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        disabled={loading}
                      >
                        {csvHeaders.map((header) => (
                          <option key={header} value={header}>
                            {header}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Source Tag
                      </label>
                      <input
                        type="text"
                        value={sourceTag}
                        onChange={(e) => setSourceTag(e.target.value)}
                        placeholder="csv-upload"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        disabled={loading}
                      />
                    </div>
                  </div>
                )}

                {/* CSV Preview */}
                {csvContent && (
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <label className="block text-sm font-medium text-gray-700">
                        CSV Preview ({csvHeaders.length} columns)
                      </label>
                      <button
                        type="button"
                        onClick={() => setShowCsvPreview(!showCsvPreview)}
                        className="flex items-center space-x-1 text-sm text-gray-600 hover:text-gray-800"
                      >
                        {showCsvPreview ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                        <span>{showCsvPreview ? 'Hide' : 'Show'} Preview</span>
                      </button>
                    </div>
                    
                    {showCsvPreview && (
                      <pre className="text-xs bg-gray-100 p-3 rounded border max-h-32 overflow-auto">
                        {getCsvPreview()}
                      </pre>
                    )}
                  </div>
                )}

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={loading || !selectedIndex || !csvContent || !contentColumn}
                  className="w-full flex items-center justify-center space-x-2 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 transition-colors"
                >
                  {loading ? (
                    <>
                      <RefreshCw className="h-5 w-5 animate-spin" />
                      <span>Uploading...</span>
                    </>
                  ) : (
                    <>
                      <Upload className="h-5 w-5" />
                      <span>Upload CSV</span>
                    </>
                  )}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminPanel;
