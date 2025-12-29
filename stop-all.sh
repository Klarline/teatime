#!/bin/bash

echo "Stopping TeaTime services..."

# Stop processes on specific ports
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null ; then
    echo "Stopping Java service (port 8081)..."
    lsof -ti:8081 | xargs kill -9
    echo "Java service stopped"
fi

if lsof -Pi :8000 -sTCP:LISTEN -t >/dev/null ; then
    echo "Stopping Python service (port 8000)..."
    lsof -ti:8000 | xargs kill -9
    echo "Python service stopped"
fi

if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
    echo "Stopping Frontend (port 3000)..."
    lsof -ti:3000 | xargs kill -9
    echo "Frontend stopped"
fi

echo ""
echo "All TeaTime services stopped"