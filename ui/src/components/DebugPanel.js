import React, { useState } from 'react';
import ragApi from '../services/ragApi';

const DebugPanel = () => {
  const [healthStatus, setHealthStatus] = useState('unknown');
  const [searchResult, setSearchResult] = useState(null);
  const [logs, setLogs] = useState([]);

  const addLog = (message) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs(prev => [...prev, `[${timestamp}] ${message}`]);
  };

  const testHealthCheck = async () => {
    addLog('Testing health check...');
    try {
      const result = await ragApi.healthCheck();
      setHealthStatus('up');
      addLog(`✅ Health check successful: ${JSON.stringify(result)}`);
    } catch (error) {
      setHealthStatus('down');
      addLog(`❌ Health check failed: ${error.message}`);
      if (error.response) {
        addLog(`Response status: ${error.response.status}`);
        addLog(`Response data: ${JSON.stringify(error.response.data)}`);
      }
    }
  };

  const testSearchWithEmbeddings = async () => {
    addLog('Testing search with embeddings...');
    try {
      const result = await ragApi.searchDocuments('test', 'summarization-test', {
        size: 3,
        includeEmbeddings: true
      });
      setSearchResult(result);
      addLog(`✅ Search successful! Found ${result.totalResults} results`);
      
      if (result.results && result.results.length > 0) {
        const hasEmbedding = !!result.results[0].document.embedding;
        addLog(`First result has embedding: ${hasEmbedding}`);
        if (hasEmbedding) {
          addLog(`Embedding dimensions: ${result.results[0].document.embedding.length}`);
        }
      }
    } catch (error) {
      addLog(`❌ Search failed: ${error.message}`);
    }
  };

  const clearLogs = () => {
    setLogs([]);
    setSearchResult(null);
  };

  return (
    <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-8">
      <h3 className="text-lg font-semibold text-yellow-800 mb-4">Debug Panel</h3>
      
      <div className="flex space-x-4 mb-4">
        <button 
          onClick={testHealthCheck}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Test Health Check
        </button>
        <button 
          onClick={testSearchWithEmbeddings}
          className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Test Search with Embeddings
        </button>
        <button 
          onClick={clearLogs}
          className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
        >
          Clear Logs
        </button>
      </div>

      <div className="mb-4">
        <strong>Health Status:</strong> 
        <span className={`ml-2 px-2 py-1 rounded text-sm ${
          healthStatus === 'up' ? 'bg-green-100 text-green-800' :
          healthStatus === 'down' ? 'bg-red-100 text-red-800' : 
          'bg-gray-100 text-gray-800'
        }`}>
          {healthStatus}
        </span>
      </div>

      {searchResult && (
        <div className="mb-4 p-3 bg-white rounded border">
          <strong>Last Search Result:</strong>
          <pre className="text-xs mt-2 bg-gray-100 p-2 rounded overflow-x-auto">
            {JSON.stringify(searchResult, null, 2)}
          </pre>
        </div>
      )}

      <div className="bg-gray-900 text-green-400 p-3 rounded font-mono text-sm max-h-64 overflow-y-auto">
        <strong className="text-green-300">Debug Logs:</strong>
        {logs.length === 0 ? (
          <div className="text-gray-500 mt-2">No logs yet. Click the test buttons above.</div>
        ) : (
          logs.map((log, index) => (
            <div key={index} className="mt-1">{log}</div>
          ))
        )}
      </div>
    </div>
  );
};

export default DebugPanel;
