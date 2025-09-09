#!/bin/bash

# Delete all Java RAG stack resources from Kubernetes
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

cd "$(dirname "$0")/.."

echo -e "${BLUE}Deleting Java RAG stack from Kubernetes...${NC}"

# Check kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}kubectl command not found. Please install kubectl.${NC}"
    exit 1
fi

# Check cluster connection
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}Cannot connect to Kubernetes cluster. Please check your kubeconfig.${NC}"
    exit 1
fi

# Delete app first
echo -e "${YELLOW}Deleting Java RAG Service...${NC}"
kubectl delete -f k8s/app/ --ignore-not-found=true

# Delete Ollama
echo -e "${YELLOW}Deleting Ollama...${NC}"
kubectl delete -f k8s/ollama/ --ignore-not-found=true

# Delete OpenSearch
echo -e "${YELLOW}Deleting OpenSearch...${NC}"
kubectl delete -f k8s/opensearch/ --ignore-not-found=true

# Clean up any remaining jobs
echo -e "${YELLOW}Cleaning up jobs...${NC}"
kubectl delete job ollama-model-init --ignore-not-found=true

# Wait for cleanup
echo -e "${YELLOW}Waiting for resources to be cleaned up...${NC}"
sleep 10

# Show final status
echo -e "${GREEN}Cleanup complete!${NC}"
echo -e "${BLUE}Remaining resources (should be empty):${NC}"
kubectl get pods -l app=java-rag-service || true
kubectl get pods -l app=ollama || true
kubectl get pods -l app=opensearch || true
kubectl get services -l app=java-rag-service || true
kubectl get services -l app=ollama || true
kubectl get services -l app=opensearch || true
