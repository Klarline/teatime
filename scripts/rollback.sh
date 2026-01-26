#!/bin/bash
set -e

PREVIOUS_TAG=${1:-previous}

echo "Rolling back to $PREVIOUS_TAG..."

# Update docker-compose to use previous tag
sed -i "s/:latest/:$PREVIOUS_TAG/g" docker-compose.yml

# Restart services
docker-compose down
docker-compose up -d

# Health checks
sleep 30
curl -f http://localhost:8081/actuator/health || exit 1
curl -f http://localhost:8000/ai/health || exit 1

echo "Rollback successful!"