# 🛒 E-Commerce Microservices Platform

> A secure, distributed e-commerce application built with microservices architecture, featuring integrated DevSecOps pipeline.

[![DevSecOps Pipeline](https://github.com/AbdrrahimDahmani/projet-devsecops/actions/workflows/devsecops.yml/badge.svg)](https://github.com/AbdrrahimDahmani/projet-devsecops/actions/workflows/devsecops.yml)

## 👥 Authors

- **Abdrrahim Dahmani** - [@AbdrrahimDahmani](https://github.com/AbdrrahimDahmani)
- **Driss Rad** - [@drissrad](https://github.com/drissrad)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND REACT                              │
│                        (Authentification Keycloak)                       │
│                              Port: 3000                                  │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY                                   │
│                    (Spring Cloud Gateway + JWT)                          │
│                         Port: 8080                                       │
└─────────────────────────────────────────────────────────────────────────┘
                          │                    │
                          ▼                    ▼
┌─────────────────────────────────┐  ┌─────────────────────────────────────┐
│     PRODUCT SERVICE             │  │     ORDER SERVICE                   │
│        (Spring Boot)            │  │        (Spring Boot)                │
│         Port: 8081              │  │         Port: 8082                  │
│                                 │  │                                     │
│  ┌─────────────────────────┐    │  │    ┌─────────────────────────┐      │
│  │   PostgreSQL Products   │    │  │    │   PostgreSQL Orders     │      │
│  │       Port: 5432        │    │  │    │        Port: 5433       │      │
│  └─────────────────────────┘    │  │    └─────────────────────────┘      │
└─────────────────────────────────┘  └─────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                             KEYCLOAK                                     │
│                    (Identity & Access Management)                        │
│                         Port: 8180                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

## 🛠️ Tech Stack

| Component | Technology | Port |
|-----------|-----------|------|
| Frontend | React 18 + Vite + Keycloak JS | 3000 |
| API Gateway | Spring Cloud Gateway 3.2 | 8080 |
| Product Service | Spring Boot 3.2 | 8081 |
| Order Service | Spring Boot 3.2 | 8082 |
| Identity Provider | Keycloak 23 | 8180 |
| Product Database | PostgreSQL 15 | 5432 |
| Order Database | PostgreSQL 15 | 5433 |

## 🔐 Security

### Authentication & Authorization
- **OAuth2 / OpenID Connect** via Keycloak
- **JWT Token** propagation between services
- **Role-based access control** (ADMIN, CLIENT)

### API Permissions

| Endpoint | ADMIN | CLIENT |
|----------|-------|--------|
| `GET /api/products` | ✅ | ✅ |
| `GET /api/products/{id}` | ✅ | ✅ |
| `POST /api/products` | ✅ | ❌ |
| `PUT /api/products/{id}` | ✅ | ❌ |
| `DELETE /api/products/{id}` | ✅ | ❌ |
| `POST /api/orders` | ❌ | ✅ |
| `GET /api/orders/my-orders` | ❌ | ✅ |
| `GET /api/orders` | ✅ | ❌ |

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Node.js 18+
- Maven 3.8+

### Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Run in background
docker-compose up -d --build

# Stop all services
docker-compose down
```

### Access URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Keycloak Admin | http://localhost:8180/admin |

### Test Users

| User | Password | Role |
|------|----------|------|
| admin | admin123 | ADMIN |
| client | client123 | CLIENT |

Keycloak Admin: `admin` / `admin`

---

## 🔄 DevSecOps Pipeline

Our CI/CD pipeline runs automatically on every push and pull request to the `main` branch.

### Pipeline Stages

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Build &   │───▶│   OWASP     │───▶│  SonarCloud │───▶│   Docker    │───▶│   Trivy     │
│    Test     │    │ Dep Check   │    │  Analysis   │    │   Build     │    │   Scan      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

| Stage | Tool | Purpose |
|-------|------|---------|
| **Build & Test** | Maven | Compile and run unit tests |
| **OWASP Dependency Check** | OWASP DC | Scan dependencies for known CVEs |
| **SonarCloud Analysis** | SonarCloud | Static code analysis (SAST) |
| **Docker Build** | Docker | Build container images |
| **Trivy Security Scan** | Trivy | Container & filesystem vulnerability scanning |

### Security Tools

| Tool | Type | Description |
|------|------|-------------|
| **SonarCloud** | SAST | Static Application Security Testing - code quality & security |
| **OWASP Dependency-Check** | SCA | Software Composition Analysis - dependency vulnerabilities |
| **Trivy** | Container/SCA | Comprehensive vulnerability scanner for containers & code |

### Pipeline Configuration

The pipeline is defined in [`.github/workflows/devsecops.yml`](.github/workflows/devsecops.yml).

Security findings are automatically uploaded to **GitHub Security** tab (SARIF format).

---

## 📁 Project Structure

```
├── .github/
│   └── workflows/
│       └── devsecops.yml       # CI/CD Pipeline
├── api-gateway/                # Spring Cloud Gateway
├── product-service/            # Product microservice
├── order-service/              # Order microservice  
├── frontend/                   # React application
├── keycloak/                   # Keycloak realm config
├── docs/                       # Documentation
│   ├── architecture-mermaid.md
│   ├── api-documentation.md
│   ├── sequence-diagram.md
│   └── trivy-reports/
├── docker-compose.yml          # Docker orchestration
└── sonar-project.properties    # SonarCloud config
```

## 💻 Local Development

### Run Services Individually

```bash
# Product Service
cd product-service
mvn spring-boot:run

# Order Service
cd order-service
mvn spring-boot:run

# API Gateway
cd api-gateway
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

---

## 📊 Sequence Diagram - Order Creation

```
┌──────────┐     ┌───────────┐     ┌────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client  │     │ Keycloak  │     │ API Gateway│     │Order Service│     │Product Svc  │
└────┬─────┘     └─────┬─────┘     └──────┬─────┘     └──────┬──────┘     └──────┬──────┘
     │                 │                  │                  │                   │
     │  1. Login       │                  │                  │                   │
     │────────────────>│                  │                  │                   │
     │                 │                  │                  │                   │
     │  2. JWT Token   │                  │                  │                   │
     │<────────────────│                  │                  │                   │
     │                 │                  │                  │                   │
     │  3. POST /api/orders + JWT         │                  │                   │
     │───────────────────────────────────>│                  │                   │
     │                 │                  │                  │                   │
     │                 │    4. Validate JWT                  │                   │
     │                 │<─────────────────│                  │                   │
     │                 │                  │                  │                   │
     │                 │    5. JWT Valid  │                  │                   │
     │                 │─────────────────>│                  │                   │
     │                 │                  │                  │                   │
     │                 │                  │  6. Forward + JWT│                   │
     │                 │                  │─────────────────>│                   │
     │                 │                  │                  │                   │
     │                 │                  │                  │ 7. Check stock    │
     │                 │                  │                  │──────────────────>│
     │                 │                  │                  │                   │
     │                 │                  │                  │ 8. Stock OK       │
     │                 │                  │                  │<──────────────────│
     │                 │                  │                  │                   │
     │                 │                  │ 9. Order created │                   │
     │                 │                  │<─────────────────│                   │
     │                 │                  │                  │                   │
     │  10. Response   │                  │                  │                   │
     │<───────────────────────────────────│                  │                   │
```

---

## 📚 Documentation

- [Architecture Diagrams](docs/architecture-mermaid.md)
- [API Documentation](docs/api-documentation.md)
- [Sequence Diagrams](docs/sequence-diagram.md)
- [DevSecOps Guide](docs/devsecops-guide.md)

---

## 📄 License

Academic Project - Secure Distributed Application Development

**ENSET Mohammedia** - 2025/2026
