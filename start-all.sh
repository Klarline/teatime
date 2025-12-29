#!/bin/bash

echo "Starting TeaTime services..."
echo ""

# Set Java 17 for this script
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java 17: $JAVA_HOME"
echo ""

# Check if services are already running
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null ; then
    echo "WARNING: Port 8081 already in use (Java service may be running)"
    echo "         Stop it with: lsof -ti:8081 | xargs kill -9"
    exit 1
fi

if lsof -Pi :8000 -sTCP:LISTEN -t >/dev/null ; then
    echo "WARNING: Port 8000 already in use (Python service may be running)"
    echo "         Stop it with: lsof -ti:8000 | xargs kill -9"
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

# Start Java service
echo "Starting Java Spring Boot service..."
cd java-service
mvn spring-boot:run > ../logs/java-service.log 2>&1 &
JAVA_PID=$!
cd ..
echo "Java service started (PID: $JAVA_PID)"

# Wait for Java service to be ready
echo "Waiting for Java service to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:8081/api/user/code > /dev/null 2>&1; then
        echo "Java service is ready!"
        break
    fi
    sleep 2
done

# Start Python service with conda
echo ""
echo "Starting Python AI service..."
cd python-service

# Check if conda environment exists
if conda env list | grep -q "teatime-ai"; then
    echo "Using conda environment: teatime-ai"
    # Use conda run to execute in the environment
    conda run -n teatime-ai python -m app.main > ../logs/python-service.log 2>&1 &
    PYTHON_PID=$!
else
    echo "WARNING: Conda environment 'teatime-ai' not found!"
    echo "         Create it with: conda create -n teatime-ai python=3.10"
    echo "         Then install dependencies: conda activate teatime-ai && pip install -r requirements.txt"
    kill $JAVA_PID
    exit 1
fi

cd ..
echo "Python AI service started (PID: $PYTHON_PID)"

# Wait for Python service to be ready
echo "Waiting for Python AI service to be ready..."
for i in {1..20}; do
    if curl -s http://localhost:8000/health > /dev/null 2>&1; then
        echo "Python AI service is ready!"
        break
    fi
    sleep 1
done

# Start Frontend
echo ""
echo "Starting React frontend..."
cd frontend
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
echo "Frontend started (PID: $FRONTEND_PID)"

echo ""
echo "========================================"
echo "All TeaTime services are running!"
echo "========================================"
echo ""
echo "Services:"
echo "  Frontend:    http://localhost:3000"
echo "  Java API:    http://localhost:8081"
echo "  Python AI:   http://localhost:8000"
echo ""
echo "Process IDs:"
echo "  Java:    $JAVA_PID"
echo "  Python:  $PYTHON_PID"
echo "  Frontend: $FRONTEND_PID"
echo ""
echo "Logs:"
echo "  Java:    tail -f logs/java-service.log"
echo "  Python:  tail -f logs/python-service.log"
echo "  Frontend: tail -f logs/frontend.log"
echo ""
echo "To stop all services:"
echo "  Press Ctrl+C or run: ./stop-all.sh"
echo ""

# Wait for Ctrl+C
trap "echo ''; echo 'Stopping all services...'; kill $JAVA_PID $PYTHON_PID $FRONTEND_PID 2>/dev/null; echo 'All services stopped'; exit 0" EXIT INT TERM

# Keep script running
wait