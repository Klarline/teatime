#!/bin/bash
set -e

echo "🚀 Starting TeaTime services with Docker Compose..."

# Check if .env exists
if [ ! -f .env ]; then
    echo "⚠️  No .env file found. Creating from .env.example..."
    cp .env.example .env
    echo "⚠️  Please edit .env with your actual values (especially GOOGLE_API_KEY)"
    echo "   Then run this script again."
    exit 1
fi

# Load environment variables
source .env

# Check for required variables
if [ -z "$GOOGLE_API_KEY" ] || [ "$GOOGLE_API_KEY" == "your_gemini_api_key_here" ]; then
    echo "❌ GOOGLE_API_KEY not configured in .env"
    echo "   Get your key from: https://makersuite.google.com/app/apikey"
    exit 1
fi

echo "📦 Building and starting services..."
docker-compose up -d --build

echo ""
echo "⏳ Waiting for services to be healthy..."
echo ""

# Wait for MySQL
echo "   Waiting for MySQL..."
timeout 60 bash -c 'until docker-compose exec -T mysql mysqladmin ping -h localhost -u root -p$MYSQL_ROOT_PASSWORD 2>/dev/null; do sleep 2; done' || true
echo "   ✅ MySQL is ready"

# Wait for Redis
echo "   Waiting for Redis..."
timeout 30 bash -c 'until docker-compose exec -T redis redis-cli ping 2>/dev/null; do sleep 2; done' || true
echo "   ✅ Redis is ready"

# Wait for Java service
echo "   Waiting for Java service..."
timeout 90 bash -c 'until curl -sf http://localhost:8081/actuator/health > /dev/null 2>&1; do sleep 3; done' || true
echo "   ✅ Java service is ready"

# Wait for Python service
echo "   Waiting for Python AI service..."
timeout 60 bash -c 'until curl -sf http://localhost:8000/ai/health > /dev/null 2>&1; do sleep 3; done' || true
echo "   ✅ Python service is ready"

# Wait for Frontend
echo "   Waiting for Frontend..."
timeout 30 bash -c 'until curl -sf http://localhost:3000/health > /dev/null 2>&1; do sleep 2; done' || true
echo "   ✅ Frontend is ready"

echo ""
echo "✨ All services are up and running!"
echo ""
echo "🌐 Access the application:"
echo "   Frontend:     http://localhost:3000"
echo "   Java API:     http://localhost:8081/api"
echo "   Python AI:    http://localhost:8000"
echo "   API Docs:     http://localhost:8000/docs"
echo ""
echo "📊 View logs:"
echo "   All services: docker-compose logs -f"
echo "   Java only:    docker-compose logs -f java-service"
echo "   Python only:  docker-compose logs -f python-service"
echo ""
echo "🛑 Stop services:"
echo "   ./stop-all.sh"
echo ""