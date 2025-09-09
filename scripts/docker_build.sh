#!/bin/bash

# Docker build script for Java RAG Service
set -e

# Configuration
IMAGE_NAME="java-rag-service"
IMAGE_TAG="${1:-latest}"
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}Building Docker image: ${FULL_IMAGE_NAME}${NC}"

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

# Navigate to project root
cd "$(dirname "$0")/.."

# Clean previous builds (optional)
echo -e "${YELLOW}Cleaning previous Maven build...${NC}"
mvn clean -q

# Build the Docker image
echo -e "${YELLOW}Building Docker image...${NC}"
docker build -t "${FULL_IMAGE_NAME}" .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Successfully built Docker image: ${FULL_IMAGE_NAME}${NC}"
    
    # Show image details
    echo -e "${BLUE}Image details:${NC}"
    docker images "${IMAGE_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
    
    echo -e "${GREEN}To run the container: ./scripts/docker_run.sh${NC}"
    echo -e "${GREEN}To push to registry: docker push ${FULL_IMAGE_NAME}${NC}"
else
    echo -e "${RED}Failed to build Docker image${NC}"
    exit 1
fi
