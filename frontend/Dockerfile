# Multi-stage build for React frontend
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install && npm cache clean --force

# Copy source
COPY . .

# Set build-time environment variable
ENV VITE_API_BASE_URL=/api

# Build for production
RUN npm run build

# Stage 2: Production with Nginx
FROM nginx:1.25-alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Add healthcheck script
RUN echo 'wget -q --spider http://localhost:80/ && exit 0 || exit 1' > /healthcheck.sh && \
    chmod +x /healthcheck.sh

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD /healthcheck.sh

# Expose port
EXPOSE 80

# Run nginx
CMD ["nginx", "-g", "daemon off;"]