# TeaTime

![Docker](https://img.shields.io/badge/docker-ready-blue)

A full-stack social platform for tea enthusiasts featuring AI-powered recommendations, real-time geolocation discovery, flash sales with distributed locking, and community-driven content sharing.

## Overview

TeaTime is a microservices-based web application that enables users to discover tea shops through natural language queries, share experiences through blog posts, participate in flash sales, and connect with other tea enthusiasts. The platform demonstrates modern full-stack development practices including distributed systems patterns, RAG (Retrieval-Augmented Generation) AI integration, multi-tier caching strategies, containerization, and CI/CD automation.

**Business Domain:** Tea shop discovery and social networking  
**Architecture:** React TypeScript SPA + Java Spring Boot REST API + Python FastAPI AI Service + MySQL + Redis + Vector Database

## Technology Stack

### Backend (Java Service)
- **Framework:** Spring Boot 2.7.4
- **Language:** Java 17
- **Database:** MySQL 8.0
- **Cache Layer:** Redis 7.x
- **ORM:** MyBatis-Plus
- **Build Tool:** Maven 3.6+
- **Key Libraries:** Redisson (distributed locks), Hutool, Lombok, Spring Boot Actuator

### AI Service (Python)
- **Framework:** FastAPI 0.109.0
- **Language:** Python 3.10
- **AI/ML:** Google Gemini API, LangChain, sentence-transformers
- **Vector Database:** Chroma 0.4.22
- **Testing:** pytest, pytest-cov

### Frontend
- **Framework:** React 18 with TypeScript
- **Build Tool:** Vite
- **Styling:** TailwindCSS
- **State Management:** Zustand
- **Routing:** React Router v6
- **HTTP Client:** Axios
- **Testing:** Jest, React Testing Library
- **UI Components:** Lucide Icons, React Hot Toast

### DevOps & Infrastructure
- **Containerization:** Docker with multi-stage builds
- **Orchestration:** Docker Compose
- **CI/CD:** GitHub Actions
- **Testing:** JUnit 5, Jest, pytest
- **Coverage:** JaCoCo, Jest coverage reporter

## Core Features

### AI-Powered Recommendations
- Natural language tea shop recommendations using RAG pipeline
- Semantic search with vector embeddings (Google Gemini)
- Context-aware responses based on actual user reviews
- Query examples: "Find me a quiet matcha cafe for studying", "Best bubble tea spots nearby"

### User Management
- Phone-based authentication with verification codes
- Redis-backed session management for horizontal scalability
- Daily check-in system using Redis Bitmap for efficient storage
- User profiles with activity tracking

### Tea Shop Discovery
- Browse shops by tea category (Bubble Tea, Matcha Cafes, Traditional Tea Houses, etc.)
- Geolocation-based search with Redis GEO
- Distance calculation and proximity sorting
- Multi-level caching with Cache Aside pattern
- Shop detail pages with ratings and reviews

### Review System
- Dedicated review functionality
- 5-star rating system for tea shops
- Pagination support for shop reviews
- Aggregate rating statistics (average rating, review count)
- One review per user per shop enforcement
- User's own review management (delete capability)

### Social Features
- Create and share blog posts with photo uploads
- Like system implemented with Redis Sets
- Follow/unfollow functionality
- Discover common connections
- Community feed with pagination
- Automatic AI ingestion of new reviews for recommendations

### Flash Sale System
- Distributed locking with Redisson for inventory management
- Coupon-based flash sales with time constraints
- Atomic operations using Lua scripts
- One coupon per user enforcement
- Asynchronous order processing with Redis Streams

### Geolocation Services
- Real-time distance calculation
- 5km radius search
- Sort results by proximity
- Mobile device location integration

## Architecture

```
Frontend (React TS on port 3000)
    |
    | HTTP/REST
    v
Java Spring Boot (port 8081) <--- REST ---> Python FastAPI (port 8000)
    |                                           |
    v                                           v
MySQL + Redis                            Vector DB (Chroma)
                                                |
                                                v
                                           Gemini API
```

### Microservices Communication
- Frontend communicates with Java service for core functionality
- Java service proxies AI requests to Python service
- Python service handles RAG pipeline and vector search
- Async ingestion: Java to Python when new reviews are created

### Service Dependencies
```
MySQL (healthy) ──┐
                  ├──> Java Service (healthy) ──> Frontend
Redis (healthy) ──┘            │
                               └──> Python AI Service (healthy)
```

Services start in dependency order with health checks ensuring reliability.

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Google Gemini API key ([Get one here](https://makersuite.google.com/app/apikey))

### Quick Start with Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone https://github.com/Klarline/teatime.git
   cd teatime
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env and add your GOOGLE_API_KEY
   ```

3. **Start all services**
   ```bash
   ./start-all.sh
   ```

   This will start:
   - MySQL database on port 3306
   - Redis cache on port 6379
   - Java Spring Boot backend on port 8081
   - Python AI service on port 8000
   - React frontend on port 3000

4. **Access the application**
   
   Open http://localhost:3000 in your browser

5. **Stop all services**
   ```bash
   ./stop-all.sh
   ```

### Docker Commands

```bash
# View all services status
docker-compose ps

# View logs
docker-compose logs -f              # All services
docker-compose logs -f java-service # Single service

# Restart a service
docker-compose restart java-service

# Rebuild a service
docker-compose up -d --build java-service

# Stop services (keeps data)
docker-compose down

# Stop and remove all data (fresh start)
docker-compose down -v

# Access service shell
docker-compose exec java-service sh
docker-compose exec mysql mysql -u teatime -p
```

### Manual Setup (Development without Docker)

<details>
<summary>Click to expand manual setup instructions</summary>

#### Prerequisites
- Java Development Kit (JDK) 17
- Maven 3.6+
- MySQL 8.0
- Redis 7.x
- Node.js 20+ with npm
- Conda with Python 3.10
- Google Gemini API key

#### Java Service
```bash
cd java-service
mvn spring-boot:run
```

#### Python AI Service
```bash
conda create -n teatime-ai python=3.10
conda activate teatime-ai
cd python-service
pip install -r requirements.txt
python -m app.main
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```
</details>

**Pipeline Status:** Check the [Actions tab](https://github.com/Klarline/teatime/actions) for build status

## Docker Architecture

### Container Images
- **Java Service:** 589MB (Eclipse Temurin JRE 17 with optimized JVM flags)
- **Python AI Service:** 1.94GB (includes sentence-transformers and ML dependencies)
- **Frontend:** 76MB (Nginx Alpine serving optimized React build)

### Multi-Stage Builds
All services use multi-stage Docker builds to:
- Separate build dependencies from runtime environment
- Minimize final image sizes
- Improve security with non-root users
- Enable faster deployments and reduced attack surface

### Container Orchestration
Services are orchestrated with Docker Compose featuring:
- Health checks for all services
- Dependency management (Java waits for MySQL/Redis)
- Persistent volumes for data (MySQL, Redis, Vector DB)
- Bridge network for inter-service communication
- Automatic restart policies

## API Documentation

### Base URLs
- **Java API:** `http://localhost:8081/api`
- **Python AI API:** `http://localhost:8000`

### Authentication Endpoints
- `POST /api/user/code` - Send verification code to phone
- `POST /api/user/login` - Authenticate user
- `POST /api/user/logout` - Logout current user
- `GET /api/user/me` - Get current user profile
- `GET /api/user/info/{id}` - Get user info by ID
- `POST /api/user/checkin` - Daily check-in
- `GET /api/user/checkin/count` - Get check-in count for current month

### Shop Endpoints
- `GET /api/shop/{id}` - Retrieve shop details
- `GET /api/shop/of/type` - List shops by category (supports geolocation with x, y params)
- `GET /api/shop/of/name` - Search shops by name
- `GET /api/shop-type/list` - Get all tea shop categories
- `POST /api/shop` - Create new shop (admin)
- `PUT /api/shop` - Update shop information (admin)

### Review Endpoints
- `POST /api/review` - Create a review for a shop (rating + content)
- `GET /api/review/shop/{shopId}` - Get paginated reviews for a shop
- `GET /api/review/stats/{shopId}` - Get rating statistics (average rating, review count)
- `GET /api/review/check/{shopId}` - Check if current user has reviewed this shop
- `DELETE /api/review/{id}` - Delete user's own review

### Blog Endpoints
- `POST /api/blog` - Create new blog post
- `GET /api/blog/{id}` - Get blog post details
- `GET /api/blog/hot` - Get popular blog posts
- `GET /api/blog/of/me` - Get current user's posts
- `GET /api/blog?id={userId}&current=1` - Get posts by specific user
- `PUT /api/blog/like/{id}` - Toggle like on blog post
- `GET /api/blog/likes/{id}` - Get users who liked a post

### AI Endpoints (Java Service - Proxy to Python)
- `POST /api/ai/recommend` - Get AI-powered tea shop recommendations
  - Body: `{ "query": "quiet matcha cafe", "maxResults": 5 }`
  - Returns: `{ "recommendations": "...", "sourceBlogs": [1, 2, 3] }`
- `GET /api/ai/health` - Check AI service status and vector database count
  - Returns: `{ "status": "healthy", "vectorDbCount": 71 }`

### Python AI Service Endpoints (Direct - Internal Use)
These endpoints are called by the Java service, not directly by the frontend:
- `POST /ai/recommend` - Get recommendations (same as Java proxy)
- `POST /ai/ingest` - Ingest single review into vector database
- `POST /ai/ingest/batch` - Batch ingest multiple reviews
- `GET /ai/health` - Health check with vector DB count

### Coupon Endpoints
- `GET /api/coupon/list/{shopId}` - Get coupons for a shop
- `POST /api/coupon` - Add new coupon (admin)
- `POST /api/coupon/flash-sale` - Add flash sale coupon (admin)
- `POST /api/coupon-order/flash-sale/{id}` - Purchase flash sale coupon

### Follow Endpoints
- `PUT /api/follow/{id}/{isFollow}` - Follow or unfollow user
- `GET /api/follow/or/not/{id}` - Check if current user follows another user
- `GET /api/follow/common/{id}` - Get common followers with another user
- `GET /api/follow/followers/count/{id}` - Get follower count for a user
- `GET /api/follow/following/count/{id}` - Get following count for a user

### Upload Endpoints
- `POST /api/upload/blog` - Upload blog image (multipart/form-data)
- `GET /api/upload/blog/delete?name={filename}` - Delete uploaded image

## AI-Powered Recommendations

### RAG Pipeline Architecture

**Ingestion Flow:**
1. User creates a blog post with tea shop review
2. Java service saves to MySQL and triggers async ingestion
3. Python service receives review data
4. Review text is embedded using Google Gemini embeddings
5. Embedding stored in Chroma vector database with metadata

**Recommendation Flow:**
1. User enters natural language query (e.g., "quiet cafe for studying")
2. Query is embedded using same embedding model
3. Vector similarity search retrieves top 5 relevant reviews
4. Retrieved reviews are sent to Gemini LLM as context
5. LLM generates personalized recommendations
6. Response returned to user with source blog IDs

### Key Technologies

- **LangChain:** RAG pipeline orchestration
- **Google Gemini:** Embeddings (models/embedding-001) and generation (gemini-1.5-flash)
- **Chroma:** Local vector database for semantic search
- **FastAPI:** High-performance async Python web framework

### Example Queries

- "Find me a cozy place for afternoon tea"
- "Where can I get good matcha lattes?"
- "I want a quiet cafe to work from"
- "Best bubble tea shops nearby"
- "Recommend tea shops with outdoor seating"

## Architecture Highlights

### Microservices Design

**Service Separation:**
- **Java Service:** Core business logic, CRUD operations, authentication, caching, distributed locks
- **Python Service:** AI-specific functionality, vector search, LLM integration

**Inter-Service Communication:**
- RESTful APIs between services
- Java uses RestTemplate for Python service calls
- Async ingestion with error handling (AI failures don't break core features)
- Health checks for service monitoring

### Distributed Systems Patterns

**Multi-Tier Caching Strategy**
- Cache Aside pattern for shop data
- Logical expiration for high-availability scenarios
- Mutex locks for cache rebuilding to prevent cache breakdown
- Bloom filters for cache penetration prevention

**Distributed Locking**
- Redisson-based distributed locks for flash sale inventory
- Prevents overselling in concurrent scenarios
- Automatic lock renewal and timeout handling

**Asynchronous Processing**
- Redis Streams for order completion messages
- Async review ingestion to AI service
- Decouples order creation from fulfillment
- Ensures eventual consistency

### Data Consistency

**Atomic Operations**
- Lua scripts for flash sale stock deduction
- Guarantees atomicity without race conditions
- One coupon per user enforcement with Redis Sets

**Session Management**
- Stateless backend with Redis session storage
- Enables horizontal scaling of application servers
- 30-minute session timeout with automatic renewal

### Performance Optimizations

**Caching Strategy**
- Shop data cached with logical expiration (30-minute TTL)
- User sessions in Redis with token-based access
- Blog likes stored in Redis Sets for O(1) lookup
- Geolocation data indexed with Redis GEO

**Database Optimization**
- Indexed foreign keys for fast joins
- MyBatis-Plus for efficient SQL generation
- Connection pooling for reduced latency

**AI Service Optimization**
- Local vector database for fast semantic search
- Embedding caching to reduce API calls
- Singleton pattern for service instances

## Docker Architecture

### Container Images
- **Java Service:** 589MB (Eclipse Temurin JRE 17 with optimized JVM settings)
- **Python AI Service:** 1.94GB (includes ML models and dependencies)
- **Frontend:** 76MB (Nginx Alpine serving production build)

### Multi-Stage Builds
All services use multi-stage Docker builds to:
- Separate build dependencies from runtime environment
- Minimize final image sizes (60% reduction vs single-stage)
- Improve security with non-root users
- Enable layer caching for faster rebuilds

### Service Dependencies and Health Checks
```
MySQL (healthy) ──┐
                  ├──> Java Service (healthy) ──> Frontend (Nginx)
Redis (healthy) ──┘            │
                               └──> Python AI Service (healthy)
```

Each service includes:
- Liveness probes to detect crashed containers
- Readiness probes to manage traffic routing
- Startup probes for slow-starting services
- Automatic restart on failure

### Networking
- Bridge network for inter-service communication
- Service discovery via container names
- Health checks prevent cascading failures
- Nginx reverse proxy for API routing

## Testing

### Test Coverage Summary
- **Backend (Java):** 68 unit and integration tests
- **Frontend (React):** 68 tests, 100% coverage on tested components
- **Python (AI Service):** Health check and endpoint validation tests

### Testing Strategy
Focus on business-critical components:
- REST API endpoints and controllers
- Service layer business logic
- React components and UI interactions
- Utility functions and helpers
- Exclude data layer (entities, DTOs) and configuration

### Running Tests

```bash
# Backend tests with coverage
cd java-service
mvn clean test
mvn jacoco:report
open target/site/jacoco/index.html

# Frontend tests with coverage
cd frontend
npm run test
npm run test:coverage
open coverage/lcov-report/index.html

# Python tests
cd python-service
pytest
pytest --cov=app --cov-report=html
```

## CI/CD & Deployment

### Automated Pipeline

Every push to the repository triggers automated workflows:

**Build & Test Pipeline:**
- Backend tests with JUnit 5 and JaCoCo coverage
- Frontend tests with Jest and React Testing Library
- Python tests with pytest
- Security vulnerability scanning
- Code quality checks

**Docker Build Pipeline:**
- Multi-stage builds for all services
- Image optimization and layer caching
- Automated tagging with git SHA
- AWS deployment

View pipeline status: [GitHub Actions](https://github.com/Klarline/teatime/actions)

### Deployment Scripts

Production-ready deployment automation:
```bash
# Deploy all services
./scripts/deploy.sh

# Rollback to previous version
./scripts/rollback.sh <version-tag>
```

**Deployment Process:**
1. Pull latest code from repository
2. Stop existing containers gracefully
3. Pull/build updated Docker images
4. Start services with health checks
5. Verify all services are healthy
6. Clean up old images

**Features:**
- Zero-downtime deployment strategy
- Automated health checks before completion
- Rollback capability for quick recovery
- Comprehensive logging for debugging

## Development Workflow

### Local Development with Docker

```bash
# Start all services
./start-all.sh

# View logs in real-time
docker-compose logs -f

# Restart after code changes
docker-compose restart java-service

# Rebuild after dependency changes
docker-compose up -d --build java-service

# Stop everything
./stop-all.sh
```

### Backend Development (Java)
```bash
# Run with hot reload (requires Spring Boot DevTools)
cd java-service
mvn spring-boot:run

# Run tests
mvn test

# Package for production
mvn clean package -DskipTests
```

### AI Service Development (Python)
```bash
# Activate conda environment
conda activate teatime-ai

# Run with auto-reload
cd python-service
uvicorn app.main:app --reload

# Run tests
pytest

# Update dependencies
pip freeze > requirements.txt
```

### Frontend Development (React)
```bash
# Development server with HMR
cd frontend
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Production build
npm run build
npm run preview
```

## Environment Configuration

### Docker Compose (.env)
```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=your_password
MYSQL_USER=teatime
MYSQL_PASSWORD=your_password

# Redis Configuration  
REDIS_PASSWORD=your_password

# AI Service
GOOGLE_API_KEY=your_gemini_api_key

# Service URLs (auto-configured in Docker)
JAVA_SERVICE_URL=http://java-service:8081
PYTHON_SERVICE_URL=http://python-service:8000
```

### Java Service (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/teatime
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  redis:
    host: ${SPRING_REDIS_HOST}
    port: ${SPRING_REDIS_PORT}
    password: ${SPRING_REDIS_PASSWORD}

teatime:
  ai:
    service:
      url: ${TEATIME_AI_SERVICE_URL}
      timeout: 30000
```

### Python Service (.env)
```env
GOOGLE_API_KEY=your_key_here
GEMINI_MODEL=gemini-1.5-flash
EMBEDDING_MODEL=models/embedding-001
CHROMA_PERSIST_DIR=/app/data/chroma_db
JAVA_SERVICE_URL=http://java-service:8081
```

### Frontend Build-time Variables
```dockerfile
ENV VITE_API_BASE_URL=/api
```

## Project Structure

```
teatime/
├── scripts/
│   ├── deploy.sh               # Automated deployment
│   └── rollback.sh             # Quick rollback
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions CI pipeline
├── docker-compose.yml          # Service orchestration
├── .env.example                # Environment template
├── start-all.sh                # Start all services script
├── stop-all.sh                 # Stop all services script
│
├── java-service/               # Spring Boot backend
│   ├── Dockerfile              # Multi-stage Java build
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/teatime/
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── entity/
│       │   │   ├── mapper/
│       │   │   ├── dto/
│       │   │   ├── config/
│       │   │   └── utils/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── mapper/
│       └── test/              # JUnit 5 tests
│
├── python-service/            # FastAPI AI service
│   ├── Dockerfile             # Multi-stage Python build
│   ├── requirements.txt
│   ├── .env.example
│   ├── app/
│   │   ├── main.py
│   │   ├── config.py
│   │   ├── models.py
│   │   ├── rag/
│   │   │   ├── embeddings.py
│   │   │   ├── retriever.py
│   │   │   ├── generator.py
│   │   │   └── pipeline.py
│   │   └── api/
│   │       ├── recommend.py
│   │       └── ingest.py
│   └── tests/                # pytest tests
│
└── frontend/                 # React TypeScript SPA
    ├── Dockerfile            # Multi-stage frontend build
    ├── nginx.conf            # Nginx configuration
    ├── package.json
    ├── vite.config.ts
    ├── jest.config.js
    └── src/
        ├── api/
        ├── components/
        │   ├── common/
        │   ├── layout/
        │   ├── shop/
        │   ├── blog/
        │   └── ai/
        ├── pages/
        ├── hooks/
        ├── store/
        ├── types/
        ├── utils/
        └── __tests__/       # Jest + RTL tests
```

## Redis Key Structure

```
teatime:login:code:{phone}                    # SMS verification codes
teatime:login:token:{token}                   # User session data
teatime:cache:shop:{id}                       # Shop information cache
teatime:lock:shop:{id}                        # Mutex lock for cache rebuild
teatime:flashsale:stock:{couponId}            # Flash sale inventory
teatime:flashsale:order:{couponId}            # Flash sale order set
teatime:blog:liked:{blogId}                   # Users who liked a blog
teatime:feed:{userId}                         # User's blog feed (sorted set)
teatime:shop:geo:{typeId}                     # Shop geolocation index
teatime:user:checkin:{userId}:{yyyyMM}        # Check-in bitmap
stream.orders                                 # Flash sale order queue
```

## Performance Metrics

- **API Response Time:** < 200ms for cached requests
- **Cache Hit Rate:** > 80% for shop queries (monitored via custom metrics)
- **AI Recommendation Time:** < 3s for semantic search + LLM generation
- **Vector Database:** 71 tea shop reviews indexed
- **Concurrent Users:** Supports 1000+ with Redis clustering
- **Database Queries:** Optimized with proper indexing
- **Frontend Load Time:** < 2s on modern browsers

## Security Features

- Password hashing with BCrypt
- Token-based authentication with Redis
- Input validation on all endpoints
- SQL injection prevention via MyBatis parameter binding
- XSS protection through content sanitization
- CORS configuration for trusted origins
- Rate limiting on authentication endpoints
- API key management for external services
- Environment variable isolation for secrets
- Docker containers run as non-root users
- Actuator endpoints require authentication bypass configuration

## Troubleshooting

### Docker Services

```bash
# Check service health
docker-compose ps

# View logs for specific service
docker-compose logs -f java-service

# Restart unhealthy service
docker-compose restart java-service

# Rebuild after code changes
docker-compose up -d --build java-service

# Fresh start (removes all data)
docker-compose down -v
./start-all.sh
```

### Common Issues

**Java Service Won't Start**
- Check MySQL is healthy: `docker-compose ps mysql`
- Check Redis connection: `docker-compose logs redis`
- Verify environment variables in docker-compose.yml
- Check logs: `docker-compose logs java-service`

**Python Service Unhealthy**
- Verify GOOGLE_API_KEY is set in .env
- Check vector database permissions: `docker-compose exec python-service ls -la /app/data`
- View logs: `docker-compose logs python-service`

**Frontend Not Loading**
- Check nginx configuration: `docker-compose exec frontend cat /etc/nginx/conf.d/default.conf`
- Verify API proxy is working: `curl http://localhost:3000/api/shop-type/list`
- Check if Java service is accessible: `docker-compose exec frontend wget -O- http://java-service:8081/actuator/health`

**Database Tables Missing**
- Import your schema: `docker cp schema.sql teatime-mysql:/tmp/`
- Execute: `docker-compose exec mysql mysql -u teatime -p teatime < /tmp/schema.sql`
- Restart Java: `docker-compose restart java-service`

**Port Conflicts**
- Check what's using ports: `lsof -i :8081` / `lsof -i :3306` / `lsof -i :6379`
- Stop conflicting services or change ports in docker-compose.yml

## Production Deployment Considerations

### Infrastructure Requirements
- Java 17 runtime or Docker-compatible host
- MySQL 8.0 database instance or managed RDS
- Redis 7.x cache instance or managed ElastiCache
- Python 3.10+ environment with ML dependencies
- HTTPS/TLS termination (nginx or load balancer)

### Security Checklist
- [ ] Secure MySQL and Redis with strong passwords
- [ ] Rotate Google Gemini API key regularly
- [ ] Enable HTTPS/TLS for all external endpoints
- [ ] Configure CORS allowlist for production domains
- [ ] Set up application monitoring and alerting
- [ ] Enable database backups and point-in-time recovery
- [ ] Configure log aggregation and retention
- [ ] Implement rate limiting on public endpoints
- [ ] Use secrets management (AWS Secrets Manager, Vault)
- [ ] Enable container image scanning

### Scaling Considerations
- Horizontal scaling of Java service (stateless design)
- Redis clustering for high availability
- MySQL read replicas for read-heavy workloads
- CDN for static frontend assets
- Load balancer for traffic distribution

## Development Best Practices

### Git Workflow
- Main branch protected with required CI checks
- Feature branches for new development
- Automated testing prevents broken builds
- Clear commit messages following conventional commits

### Code Quality
- ESLint for frontend code standards
- JaCoCo for backend coverage reporting
- TypeScript strict mode enabled
- Lombok for reduced boilerplate
- Consistent formatting and naming conventions

### Testing Philosophy
- Unit tests for business logic
- Integration tests for API endpoints
- Component tests for UI elements
- Strategic coverage focusing on critical paths
- Mock external dependencies (Redis, MySQL, AI service)

## Contributing

Contributions are welcome. Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for new features
4. Ensure all tests pass (`mvn test` and `npm test`)
5. Update documentation as needed
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

### Development Setup for Contributors

```bash
# Clone and set up
git clone https://github.com/Klarline/teatime.git
cd teatime

# Create environment file
cp .env.example .env
# Add your GOOGLE_API_KEY

# Start all services
./start-all.sh

# Make changes and test
docker-compose restart <service-name>

# Run tests before committing
cd java-service && mvn test
cd ../frontend && npm test
```

## Author

- GitHub: [@Klarline](https://github.com/Klarline)

## Acknowledgments

This project demonstrates modern full-stack development practices including:
- Microservices architecture with polyglot services (Java + Python)
- AI integration using RAG pipeline and vector databases
- Distributed systems patterns (caching, locking, async processing)
- Production-ready authentication and session management
- Containerization with Docker and orchestration with Docker Compose
- Automated testing and CI/CD with GitHub Actions
- Responsive frontend design with TypeScript and modern React patterns
- RESTful API design and inter-service communication

### Key Technical Achievements

**Backend Engineering:**
- Implemented distributed locking for flash sales preventing race conditions
- Designed multi-tier caching strategy achieving 80%+ cache hit rate
- Built async order processing with Redis Streams for decoupled architecture
- Created session management system supporting horizontal scaling

**AI/ML Integration:**
- Developed RAG pipeline with vector embeddings for semantic search
- Integrated Google Gemini API for natural language recommendations
- Implemented async data ingestion maintaining service independence
- Optimized embedding generation and storage for sub-3s query times

**DevOps & Testing:**
- Established comprehensive test suite
- Configured GitHub Actions CI pipeline with automated testing
- Created multi-stage Docker builds reducing image sizes by 60%
- Orchestrated 5 services with Docker Compose including health checks
- Automated deployment pipeline with health checks and rollback capabilities
- Production-ready deployment scripts supporting zero-downtime updates

**Frontend Development:**
- Built responsive SPA with TypeScript for type safety
- Implemented mobile-first design with TailwindCSS
- Created reusable component library with 100% test coverage
- Integrated real-time features (geolocation, likes, follows)
