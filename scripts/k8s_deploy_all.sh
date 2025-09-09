#!/bin/bash

# Deploy the full Java RAG stack to Kubernetes (OpenSearch, Ollama, App)
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

cd "$(dirname "$0")/.."

echo -e "${BLUE}Deploying complete Java RAG stack to Kubernetes...${NC}"

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

# Deploy OpenSearch first
echo -e "${YELLOW}Deploying OpenSearch...${NC}"
kubectl apply -f k8s/opensearch/
echo -e "${YELLOW}Waiting for OpenSearch to be ready...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/opensearch

# Deploy Ollama
echo -e "${YELLOW}Deploying Ollama...${NC}"
kubectl apply -f k8s/ollama/
echo -e "${YELLOW}Waiting for Ollama to be ready...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/ollama

# Wait a bit more for Ollama to fully start before model init
sleep 30

# Deploy the app
echo -e "${YELLOW}Deploying Java RAG Service...${NC}"
kubectl apply -f k8s/app/
echo -e "${YELLOW}Waiting for app to be ready...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/java-rag-service

# Show status
echo -e "${GREEN}Full stack deployment successful!${NC}"
echo -e "${BLUE}Checking all components:${NC}"
kubectl get pods
echo -e "${BLUE}Services:${NC}"
kubectl get services

# Get external access info for the app
EXTERNAL_IP=$(kubectl get service java-rag-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
if [ -n "$EXTERNAL_IP" ]; then
    echo -e "${GREEN}Java RAG Service available at: http://${EXTERNAL_IP}${NC}"
else
    echo -e "${YELLOW}External IP pending. Check with: kubectl get service java-rag-service${NC}"
    echo -e "${YELLOW}For local access, use: kubectl port-forward service/java-rag-service 8080:80${NC}"
fi

echo -e "${BLUE}Health check endpoints:${NC}"
echo -e "  - App health: GET /actuator/health"
echo -e "  - OpenSearch: GET /_cluster/health"
echo -e "  - Ollama: GET /api/tags"

echo -e "${YELLOW}Note: Ollama model initialization may take several minutes to complete.${NC}"
echo -e "${YELLOW}Check job status with: kubectl get jobs${NC}"
