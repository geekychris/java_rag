#!/bin/bash

# RAG Service CSV Indexing Script
# This script indexes all example CSV files into the RAG service
# Server should be running on port 8080

set -e  # Exit on any error

# Configuration
RAG_SERVER="http://localhost:8080"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXAMPLES_DIR="$SCRIPT_DIR/examples"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to check if server is running
check_server() {
    print_status "Checking if RAG server is running on $RAG_SERVER..."
    if curl -s "$RAG_SERVER/actuator/health" > /dev/null 2>&1; then
        print_success "Server is running and healthy"
    else
        print_error "Server is not responding at $RAG_SERVER"
        print_error "Please make sure the RAG service is running on port 8080"
        exit 1
    fi
}

# Function to escape CSV content for JSON
escape_for_json() {
    local csv_content="$1"
    # Replace literal newlines with \n and escape double quotes
    echo "$csv_content" | sed -e 'H;1h' -e '$!d' -e 'x' -e 's/\n/\\n/g' | sed 's/"/\\"/g'
}

# Function to read CSV file and create JSON request
create_json_request() {
    local csv_file="$1"
    local index_name="$2"
    local content_column="$3"
    local source_name="$4"
    
    if [[ ! -f "$csv_file" ]]; then
        print_error "CSV file not found: $csv_file"
        return 1
    fi
    
    local csv_content=$(cat "$csv_file")
    local escaped_content=$(escape_for_json "$csv_content")
    
    cat << EOF
{
  "csvContent": "$escaped_content",
  "indexName": "$index_name",
  "contentColumnName": "$content_column",
  "source": "$source_name"
}
EOF
}

# Function to index a CSV file
index_csv_file() {
    local csv_file="$1"
    local index_name="$2"
    local content_column="$3"
    local source_name="$4"
    local description="$5"
    
    print_status "Indexing $description..."
    print_status "  File: $(basename "$csv_file")"
    print_status "  Index: $index_name"
    print_status "  Content Column: $content_column"
    
    # Create JSON request
    local json_request=$(create_json_request "$csv_file" "$index_name" "$content_column" "$source_name")
    
    # Make the API call
    local response=$(curl -s -X POST "$RAG_SERVER/api/rag/documents/csv" \
        -H "Content-Type: application/json" \
        -d "$json_request")
    
    # Check if the request was successful
    if echo "$response" | grep -q '"success":true'; then
        local doc_count=$(echo "$response" | grep -o '"documentsIngested":[0-9]*' | cut -d':' -f2)
        print_success "Successfully indexed $doc_count documents from $description"
        
        # Show headers info
        local headers=$(echo "$response" | grep -o '"headers":\[[^]]*\]' | sed 's/"headers":/Headers: /')
        print_status "  $headers"
        echo
    else
        print_error "Failed to index $description"
        print_error "Response: $response"
        echo
        return 1
    fi
}

# Function to test search functionality
test_search() {
    local index_name="$1"
    local query="$2"
    local description="$3"
    
    print_status "Testing search in $index_name with query: '$query'"
    
    local search_request='{
        "query": "'"$query"'",
        "indexName": "'"$index_name"'",
        "size": 2,
        "minScore": 0.0
    }'
    
    local response=$(curl -s -X POST "$RAG_SERVER/api/rag/search" \
        -H "Content-Type: application/json" \
        -d "$search_request")
    
    if echo "$response" | grep -q '"success":true'; then
        local result_count=$(echo "$response" | grep -o '"totalResults":[0-9]*' | cut -d':' -f2)
        print_success "Found $result_count results for '$query' in $description"
    else
        print_warning "Search test failed for $index_name"
    fi
}

# Main execution
main() {
    echo "========================================"
    echo "RAG Service CSV Indexing Script"
    echo "========================================"
    echo
    
    # Check if server is running
    check_server
    echo
    
    # Index Tech Articles
    if [[ -f "$EXAMPLES_DIR/tech_articles.csv" ]]; then
        index_csv_file \
            "$EXAMPLES_DIR/tech_articles.csv" \
            "tech-articles" \
            "content" \
            "tech-articles-csv" \
            "Technology Articles"
    else
        print_warning "tech_articles.csv not found in examples directory"
    fi
    
    # Index Research Papers  
    if [[ -f "$EXAMPLES_DIR/research_papers.csv" ]]; then
        index_csv_file \
            "$EXAMPLES_DIR/research_papers.csv" \
            "research-papers" \
            "content" \
            "research-papers-csv" \
            "Research Papers"
    else
        print_warning "research_papers.csv not found in examples directory"
    fi
    
    # Index FAQ Documents
    if [[ -f "$EXAMPLES_DIR/faq_documents.csv" ]]; then
        index_csv_file \
            "$EXAMPLES_DIR/faq_documents.csv" \
            "faq-documents" \
            "content" \
            "faq-documents-csv" \
            "FAQ Documents"
    else
        print_warning "faq_documents.csv not found in examples directory"
    fi
    
    print_success "All CSV files have been processed!"
    echo
    
    # Test searches
    print_status "Running search tests..."
    echo
    
    test_search "tech-articles" "machine learning" "Technology Articles"
    test_search "research-papers" "quantum computing" "Research Papers"  
    test_search "faq-documents" "password reset" "FAQ Documents"
    
    echo
    print_success "Indexing complete! Your RAG service now contains:"
    print_success "  • Technology articles in 'tech-articles' index"
    print_success "  • Research papers in 'research-papers' index"
    print_success "  • FAQ documents in 'faq-documents' index"
    echo
    print_status "You can now search these indices using the /api/rag/search endpoint"
    print_status "Example search:"
    echo "  curl -X POST $RAG_SERVER/api/rag/search \\"
    echo '    -H "Content-Type: application/json" \'
    echo '    -d '\''{"query": "machine learning", "indexName": "tech-articles", "size": 3}'\'''
}

# Run the main function
main "$@"
