#!/bin/bash
set -e

echo "Starting TeaTime deployment..."

# Load environment variables (skip comments and empty lines)
if [ -f .env ]; then
  export $(grep -v '^#' .env | grep -v '^$' | xargs)
fi

# Pull latest changes
echo "Pulling latest code..."
git pull origin main

# Stop old containers
echo "Stopping old containers..."
docker-compose down

# Pull latest images
echo "Pulling Docker images..."
docker-compose pull

# Start new containers
echo "Starting new containers..."
docker-compose up -d

# Wait for services to be healthy
echo "Waiting for services to start..."
sleep 30

# Health checks
echo "Running health checks..."
JAVA_HEALTH=$(curl -s http://localhost:8081/actuator/health | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
PYTHON_HEALTH=$(curl -s http://localhost:8000/ai/health | jq -r '.status' 2>/dev/null || echo "unknown")

if [ "$JAVA_HEALTH" != "UP" ]; then
  echo "Java service health check failed! Status: $JAVA_HEALTH"
  docker-compose logs --tail=50 java-service
  exit 1
fi

if [ "$PYTHON_HEALTH" != "healthy" ]; then
  echo "Python service health check failed! Status: $PYTHON_HEALTH"
  docker-compose logs --tail=50 python-service
  exit 1
fi

echo "Deployment successful!"
echo "Java service: $JAVA_HEALTH"
echo "Python service: $PYTHON_HEALTH"

# Cleanup
docker image prune -f