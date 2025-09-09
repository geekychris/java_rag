#!/bin/bash

# Run the Java RAG Service container locally
set -e

IMAGE_NAME="java-rag-service"
IMAGE_TAG="${1:-latest}"
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"

# Ports
HOST_PORT_APP=${HOST_PORT_APP:-8080}
HOST_PORT_OPENSEARCH=${HOST_PORT_OPENSEARCH:-30920}
HOST_PORT_OLLAMA=${HOST_PORT_OLLAMA:-11434}

# Environment
OPENSEARCH_HOST=${OPENSEARCH_HOST:-localhost}
OPENSEARCH_PORT=${OPENSEARCH_PORT:-${HOST_PORT_OPENSEARCH}}
OLLAMA_BASE_URL=${OLLAMA_BASE_URL:-http://localhost:${HOST_PORT_OLLAMA}}
JAVA_OPTS=${JAVA_OPTS:--Xmx1g -Xms512m}

# Ensure Docker is running
if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running. Please start Docker and try again." >&2
  exit 1
fi

# Suggest starting dependencies if needed
cat <<EOF
Note: This app expects OpenSearch on ${OPENSEARCH_HOST}:${OPENSEARCH_PORT} and Ollama at ${OLLAMA_BASE_URL}.
You can run dev dependencies with:
  OpenSearch:
    docker run -d --name opensearch-dev -p ${HOST_PORT_OPENSEARCH}:9200 -e "discovery.type=single-node" -e "plugins.security.disabled=true" opensearchproject/opensearch:2.11.1
  Ollama:
    docker run -d --name ollama-dev -p ${HOST_PORT_OLLAMA}:11434 ollama/ollama:latest
    # Then exec to pull a model: docker exec -it ollama-dev ollama pull llama2
EOF

# Run container
exec docker run --rm -it \
  -p ${HOST_PORT_APP}:8080 \
  -e OPENSEARCH_HOST=${OPENSEARCH_HOST} \
  -e OPENSEARCH_PORT=${OPENSEARCH_PORT} \
  -e OLLAMA_BASE_URL=${OLLAMA_BASE_URL} \
  -e JAVA_OPTS="${JAVA_OPTS}" \
  --name java-rag-service ${FULL_IMAGE_NAME}

