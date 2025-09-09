# Multi-stage build for Java RAG Service
# Stage 1: Build the application
FROM maven:3.9.5-amazoncorretto-21 AS build

WORKDIR /app

# Copy POM and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM amazoncorretto:21-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build --chown=spring:spring /app/target/rag-service-0.0.1-SNAPSHOT.jar app.jar

# Environment variables with defaults
ENV JAVA_OPTS="-Xmx1g -Xms512m"
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8080
ENV OPENSEARCH_HOST=opensearch-service
ENV OPENSEARCH_PORT=9200
ENV OLLAMA_BASE_URL=http://ollama-service:11434

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Expose port
EXPOSE ${SERVER_PORT}

# Run the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
