const axios = require('axios');

async function testHealthCheck() {
    console.log('Testing health check...');
    
    try {
        const response = await axios.get('http://localhost:8080/actuator/health', {
            timeout: 5000,
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('✅ Health check successful!');
        console.log('Status:', response.status);
        console.log('Data:', response.data);
        console.log('Headers:', response.headers);
        
    } catch (error) {
        console.log('❌ Health check failed!');
        console.log('Error code:', error.code);
        console.log('Error message:', error.message);
        
        if (error.response) {
            console.log('Response status:', error.response.status);
            console.log('Response data:', error.response.data);
        }
    }
}

async function testSearch() {
    console.log('\nTesting search with embeddings...');
    
    try {
        const response = await axios.post('http://localhost:8080/api/rag/search', {
            query: 'test',
            indexName: 'summarization-test',
            size: 5,
            includeEmbeddings: true
        }, {
            timeout: 10000,
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('✅ Search successful!');
        console.log('Total results:', response.data.totalResults);
        
        if (response.data.results && response.data.results.length > 0) {
            const firstResult = response.data.results[0];
            console.log('First result has embedding:', !!firstResult.document.embedding);
            
            if (firstResult.document.embedding) {
                console.log('Embedding dimensions:', firstResult.document.embedding.length);
                console.log('First few values:', firstResult.document.embedding.slice(0, 5));
            }
        }
        
    } catch (error) {
        console.log('❌ Search failed!');
        console.log('Error:', error.message);
    }
}

// Run tests
testHealthCheck().then(() => testSearch());
