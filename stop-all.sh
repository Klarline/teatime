#!/bin/bash
set -e

echo "🛑 Stopping TeaTime services..."

docker-compose down

echo "✅ All services stopped"
echo ""
echo "💾 Data is preserved in Docker volumes"
echo "   To remove volumes: docker-compose down -v"
echo ""