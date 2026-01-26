# Multi-stage build for Python FastAPI AI service
# Stage 1: Builder
FROM python:3.10-slim AS builder

WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install to user directory
COPY requirements.txt .
RUN pip install --user --no-cache-dir --no-warn-script-location -r requirements.txt

# Stage 2: Runtime
FROM python:3.10-slim

WORKDIR /app

# Install runtime dependencies only
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r teatime && useradd -r -g teatime teatime
RUN mkdir -p /home/teatime/.cache && \
    chown -R teatime:teatime /home/teatime

# Copy Python packages from builder
COPY --from=builder /root/.local /home/teatime/.local

# Copy application code
COPY app ./app

# Create data directory for vector DB
RUN mkdir -p /app/data && chown -R teatime:teatime /app

# Switch to non-root user
USER teatime

# Add local Python packages to PATH
ENV PATH=/home/teatime/.local/bin:$PATH \
    PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8000/ai/health || exit 1

# Run FastAPI with uvicorn
CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "2"]