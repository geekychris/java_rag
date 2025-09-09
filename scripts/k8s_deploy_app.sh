#!/bin/bash

# Deploy only the Java RAG Service to Kubernetes
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

cd "$(dirname "$0")/.."

echo -e "${BLUE}Deploying Java RAG Service to Kubernetes...${NC}"

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

# Apply app manifests
echo -e "${YELLOW}Applying app deployment and service...${NC}"
kubectl apply -f k8s/app/

# Wait for deployment
echo -e "${YELLOW}Waiting for deployment to be ready...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/java-rag-service

# Get service info
echo -e "${GREEN}Deployment successful!${NC}"
kubectl get pods -l app=java-rag-service
echo -e "${BLUE}Service details:${NC}"
kubectl get service java-rag-service

# Get external access info
EXTERNAL_IP=$(kubectl get service java-rag-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
if [ -n "$EXTERNAL_IP" ]; then
    echo -e "${GREEN}Service available at: http://${EXTERNAL_IP}${NC}"
else
    echo -e "${YELLOW}External IP pending. Check with: kubectl get service java-rag-service${NC}"
    echo -e "${YELLOW}For local access, use: kubectl port-forward service/java-rag-service 8080:80${NC}"
fi
