# TeaTime 🍵

A social platform for tea enthusiasts to discover, review, and share tea experiences.

## About This Project

TeaTime is a full-stack social platform application focused on tea shop discovery and social sharing.

**Business Domain:** Tea shop discovery, reviews, and social features
**Architecture:** Spring Boot backend + Redis caching + MySQL database

## Technology Stack

- **Backend:** Java 11, Spring Boot 2.7.12
- **Database:** MySQL 8.0 (teatime database)
- **Cache:** Redis 7.x
- **ORM:** MyBatis-Plus
- **Build Tool:** Maven

## Current Features

**User Management**

- Phone-based authentication with verification codes
- Redis-based session management
- Daily check-in system with Redis Bitmap

**Tea Shop Discovery**

- Browse shops by tea category (Bubble Tea, Matcha, Traditional Tea Houses, etc.)
- Geolocation-based search using Redis GEO
- Shop caching with Cache Aside pattern

**Social Features**

- Blog posts (tea stories/reviews)
- Like system with Redis Sets
- Follow/unfollow users
- Common follows discovery

**Flash Sales**

- Distributed locking with Redisson
- Coupon flash sale system
- Async order processing with Redis Streams

## Project Structure

```
java-service/
├── src/main/java/com/teatime/
│   ├── controller/      # REST API controllers (/api/*)
│   ├── service/         # Business logic
│   ├── entity/          # Database entities
│   ├── mapper/          # MyBatis mappers
│   ├── dto/             # Data transfer objects
│   ├── config/          # Spring configuration
│   └── utils/           # Utilities & helpers
├── src/main/resources/
│   ├── application.yaml # Configuration
│   └── mapper/          # MyBatis XML mappers
└── pom.xml
```

## Quick Start

### Prerequisites

- Java 11+
- MySQL 8.0
- Redis 7.x
- Maven 3.6+

### Setup

1. **Clone the repository**

```bash
git clone https://github.com/Klarline/teatime.git
cd teatime/java-service
```

2. **Configure database**

```bash
# Create database
mysql -u root -p
CREATE DATABASE teatime;

```

3. **Update application.yaml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/teatime
    username: your_username
    password: your_password
  redis:
    host: 127.0.0.1
    port: 6379
```

4. **Run the application**

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8081`

## API Endpoints

All endpoints are prefixed with `/api`:

**User Management:**

- `POST /api/user/code` - Send verification code
- `POST /api/user/login` - User login
- `POST /api/user/checkin` - Daily check-in
- `GET /api/user/me` - Get current user

**Shop Discovery:**

- `GET /api/shop/{id}` - Get shop details
- `GET /api/shop/of/type` - Get shops by tea type
- `GET /api/shop-type/list` - Get all tea categories

**Social Features:**

- `POST /api/blog` - Create blog post
- `PUT /api/blog/like/{id}` - Like a blog
- `PUT /api/follow/{id}/{isFollow}` - Follow/unfollow user

**Flash Sales:**

- `POST /api/coupon/flash-sale` - Create flash sale coupon
- `POST /api/coupon-order/flash-sale/{id}` - Purchase flash sale coupon

## Key Technical Implementations

### Distributed Systems Patterns

- **Redis Distributed Locks:** For flash sale inventory control
- **Cache Aside Pattern:** Multi-level caching strategy
- **Logical Expiration:** Prevent cache breakdown during high traffic
- **Lua Scripts:** Atomic operations for flash sales

### High-Concurrency Solutions

- Redis-based session management for horizontal scaling
- Optimistic locking for order processing
- Async message processing with Redis Streams
- Bloom filter for cache penetration prevention

## Architecture Highlights

```
Client → Spring Boot (Port 8081) → MySQL + Redis
```

**Redis Keys Structure:**

- `teatime:login:token:{token}` - User sessions
- `teatime:cache:shop:{id}` - Shop caching
- `teatime:flashsale:stock:{id}` - Flash sale inventory
- `teatime:blog:liked:{id}` - Blog likes
- `teatime:user:checkin:{userId}:{yyyyMM}` - Check-in records

## Contributing

Suggestions and improvements are welcome!

## Author

- GitHub: [@Klarline](https://github.com/Klarline)

---