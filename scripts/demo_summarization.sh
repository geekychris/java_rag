#!/bin/bash

# RAG Service Summarization Demo Script
# A simple demonstration of the summarization functionality

set -e

# Configuration
RAG_SERVICE_URL="${RAG_SERVICE_URL:-http://localhost:8080}"
INDEX_NAME="demo-summarization"

# Colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    RAG Summarization Demo              ${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# Check if service is running
echo -e "${YELLOW}Checking RAG service health...${NC}"
if ! curl -s "$RAG_SERVICE_URL/actuator/health" > /dev/null; then
    echo -e "${RED}❌ RAG service is not running at $RAG_SERVICE_URL${NC}"
    echo -e "${YELLOW}Please start the service first:${NC}"
    echo "   mvn spring-boot:run"
    echo "   # or"
    echo "   ./scripts/docker_build.sh && ./scripts/docker_run.sh"
    exit 1
fi

echo -e "${GREEN}✅ RAG service is running${NC}"
echo

# Clean up any previous demo index
echo -e "${YELLOW}Cleaning up previous demo data...${NC}"
curl -s -X DELETE "$RAG_SERVICE_URL/api/indexes/$INDEX_NAME" > /dev/null || true
echo

# Create demo data
echo -e "${YELLOW}Setting up demo data...${NC}"

# Create index
curl -s -X POST "$RAG_SERVICE_URL/api/indexes/$INDEX_NAME" > /dev/null

# Upload sample documents about AI and technology with repeated keywords
cat > /tmp/demo_data.json << 'EOF'
{
  "csvContent": "title,content,category\n\"Machine Learning Guide\",\"Machine learning algorithms enable computers to learn patterns from data automatically. Machine learning techniques include supervised learning, unsupervised learning, and reinforcement learning. Machine learning is essential for artificial intelligence applications and data science.\",\"AI\"\n\"Neural Networks Deep Dive\",\"Neural networks are machine learning models inspired by biological neural networks. These artificial neural networks process information through connected nodes. Deep neural networks with multiple layers are powerful for machine learning tasks like pattern recognition.\",\"AI\"\n\"Programming with Python\",\"Python programming language is popular for software development and data analysis. Python syntax is simple making Python programming accessible. Python libraries and frameworks make Python ideal for machine learning, web development, and programming automation tasks.\",\"Programming\"\n\"JavaScript Programming\",\"JavaScript programming is essential for web development and interactive websites. JavaScript code enables dynamic user interfaces and programming interactive features. Modern JavaScript frameworks make programming web applications more efficient and powerful.\",\"Programming\"\n\"Artificial Intelligence Revolution\",\"Artificial intelligence systems perform tasks requiring human intelligence. AI applications include machine learning, natural language processing, and computer vision. Modern artificial intelligence uses neural networks and machine learning to solve complex problems.\",\"AI\"\n\"Technology Innovation\",\"Technology innovation drives advances in artificial intelligence, machine learning, and programming. Modern technology includes cloud computing, mobile development, and data science. These technology innovations transform how we work with digital systems.\",\"Technology\"",
  "indexName": "demo-summarization",
  "contentColumnName": "content",
  "source": "demo-data"
}
EOF

echo "Uploading demo documents..."
curl -s -X POST "$RAG_SERVICE_URL/api/rag/documents/csv" \
  -H "Content-Type: application/json" \
  -d @/tmp/demo_data.json > /dev/null

echo "Waiting for indexing to complete..."
sleep 8
echo

# Demo 1: Search and Summarize
echo -e "${CYAN}📋 Demo 1: Search and Summarize in One Call${NC}"
echo -e "${YELLOW}Query: 'machine learning artificial intelligence'${NC}"
echo

response=$(curl -s -X POST "$RAG_SERVICE_URL/api/rag/search-and-summarize" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning artificial intelligence",
    "indexName": "demo-summarization",
    "size": 5,
    "minScore": 0.01
  }')

summary=$(echo "$response" | jq -r '.summary // "No summary available"')
results_count=$(echo "$response" | jq -r '.totalResults // 0')
processing_time=$(echo "$response" | jq -r '.processingTimeMs // 0')

echo -e "${GREEN}Results found: $results_count${NC}"
echo -e "${GREEN}Processing time: ${processing_time}ms${NC}"
echo
echo -e "${CYAN}AI-Generated Summary:${NC}"
echo "$summary" | fold -w 80 -s
echo
echo "---"
echo

# Demo 2: Custom Summarization
echo -e "${CYAN}📋 Demo 2: Custom Summarization with Specific Instructions${NC}"
echo -e "${YELLOW}Query: 'programming languages python javascript'${NC}"
echo -e "${YELLOW}Custom instruction: 'Focus on practical benefits for beginners'${NC}"
echo

# First get search results
search_response=$(curl -s -X POST "$RAG_SERVICE_URL/api/rag/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "programming languages python javascript",
    "indexName": "demo-summarization",
    "size": 3,
    "minScore": 0.01
  }')

search_results=$(echo "$search_response" | jq '.results')

# Then summarize with custom parameters
summary_response=$(curl -s -X POST "$RAG_SERVICE_URL/api/rag/summarize" \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"programming languages python javascript\",
    \"searchResults\": $search_results,
    \"maxSummaryLength\": 150,
    \"includeSourceReferences\": true,
    \"customPrompt\": \"Focus on practical benefits for beginners\"
  }")

custom_summary=$(echo "$summary_response" | jq -r '.summary // "No summary available"')
source_refs=$(echo "$summary_response" | jq -r '.sourceReferences[]?' | tr '\n' ', ' | sed 's/,$//')
custom_processing_time=$(echo "$summary_response" | jq -r '.processingTimeMs // 0')

echo -e "${GREEN}Processing time: ${custom_processing_time}ms${NC}"
echo -e "${GREEN}Sources: $source_refs${NC}"
echo
echo -e "${CYAN}Custom AI Summary:${NC}"
echo "$custom_summary" | fold -w 80 -s
echo
echo "---"
echo

# Demo 3: Performance Test
echo -e "${CYAN}📋 Demo 3: Performance with Broader Query${NC}"
echo -e "${YELLOW}Query: 'technology programming artificial intelligence'${NC}"
echo

start_time=$(date +%s%N)

broad_response=$(curl -s -X POST "$RAG_SERVICE_URL/api/rag/search-and-summarize" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "technology programming artificial intelligence",
    "indexName": "demo-summarization",
    "size": 10,
    "minScore": 0.001
  }')

end_time=$(date +%s%N)
client_time=$(((end_time - start_time) / 1000000))

broad_summary=$(echo "$broad_response" | jq -r '.summary // "No summary available"')
broad_results=$(echo "$broad_response" | jq -r '.totalResults // 0')
broad_server_time=$(echo "$broad_response" | jq -r '.processingTimeMs // 0')

echo -e "${GREEN}Results processed: $broad_results${NC}"
echo -e "${GREEN}Server processing time: ${broad_server_time}ms${NC}"
echo -e "${GREEN}Total client time: ${client_time}ms${NC}"
echo
echo -e "${CYAN}Comprehensive Summary:${NC}"
echo "$broad_summary" | fold -w 80 -s
echo
echo "---"
echo

# Cleanup
echo -e "${YELLOW}Cleaning up demo data...${NC}"
curl -s -X DELETE "$RAG_SERVICE_URL/api/indexes/$INDEX_NAME" > /dev/null || true
rm -f /tmp/demo_data.json

echo
echo -e "${GREEN}🎉 Demo completed successfully!${NC}"
echo
echo -e "${CYAN}Key Takeaways:${NC}"
echo "• The summarization feature can process search results and generate concise summaries"
echo "• Custom prompts allow you to tailor summaries for specific needs"
echo "• Both convenience endpoints and detailed configuration options are available"
echo "• Processing times are reasonable for real-world applications"
echo
echo -e "${YELLOW}Try it yourself:${NC}"
echo "• Bash script: ./scripts/test_summarization.sh"
echo "• Java tests: mvn test -Dtest=SummarizationIntegrationTest -Dintegration.test.enabled=true"
echo "• API endpoints: POST /api/rag/summarize, POST /api/rag/search-and-summarize"
echo
