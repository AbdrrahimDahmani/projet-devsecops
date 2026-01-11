# Application Micro-services Sécurisée

## Architecture

Application de gestion de produits et commandes basée sur une architecture micro-services sécurisée.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND REACT                              │
│                        (Authentification Keycloak)                       │
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
│     MICRO-SERVICE PRODUIT       │  │     MICRO-SERVICE COMMANDE          │
│        (Spring Boot)            │  │        (Spring Boot)                │
│         Port: 8081              │  │         Port: 8082                  │
│                                 │  │                                     │
│  ┌─────────────────────────┐    │  │    ┌─────────────────────────┐      │
│  │   PostgreSQL Produits   │    │  │    │   PostgreSQL Commandes  │      │
│  │       Port: 5432        │    │  │    │        Port: 5433       │      │
│  └─────────────────────────┘    │  │    └─────────────────────────┘      │
└─────────────────────────────────┘  └─────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                             KEYCLOAK                                     │
│                    (Serveur d'Authentification)                          │
│                         Port: 8180                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

## Composants

| Composant        | Technologie            | Port |
| ---------------- | ---------------------- | ---- |
| Frontend         | React 18 + Keycloak JS | 3000 |
| API Gateway      | Spring Cloud Gateway   | 8080 |
| Service Produit  | Spring Boot 3.2        | 8081 |
| Service Commande | Spring Boot 3.2        | 8082 |
| Keycloak         | Keycloak 23            | 8180 |
| DB Produits      | PostgreSQL 15          | 5432 |
| DB Commandes     | PostgreSQL 15          | 5433 |

## Sécurité

- **Authentification**: OAuth2 / OpenID Connect via Keycloak
- **Autorisation**: JWT avec rôles ADMIN et CLIENT
- **Propagation**: Token JWT entre services

### Rôles et Permissions

| Endpoint                         | ADMIN | CLIENT |
| -------------------------------- | ----- | ------ |
| GET /api/produits                | Oui   | Oui    |
| GET /api/produits/{id}           | Oui   | Oui    |
| POST /api/produits               | Oui   | Non    |
| PUT /api/produits/{id}           | Oui   | Non    |
| DELETE /api/produits/{id}        | Oui   | Non    |
| POST /api/commandes              | Non   | Oui    |
| GET /api/commandes/mes-commandes | Non   | Oui    |
| GET /api/commandes               | Oui   | Non    |

## Démarrage Rapide

### Prérequis

- Docker & Docker Compose
- Java 17+
- Node.js 18+
- Maven 3.8+

### Lancement avec Docker Compose

```bash
# Construire et lancer tous les services
docker-compose up --build

# Lancer en arrière-plan
docker-compose up -d --build
```

### URLs d'accès

- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Keycloak Admin: http://localhost:8180/admin (admin/admin)

### Utilisateurs de test

| Utilisateur | Mot de passe | Rôle   |
| ----------- | ------------ | ------ |
| admin       | admin123     | ADMIN  |
| client      | client123    | CLIENT |

## Développement

### Lancement individuel des services

```bash
# Backend - Service Produit
cd product-service
mvn spring-boot:run

# Backend - Service Commande
cd order-service
mvn spring-boot:run

# Backend - API Gateway
cd api-gateway
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

## DevSecOps

### Outils intégrés

| Outil     | Type          | Usage                                            |
| --------- | ------------- | ------------------------------------------------ |
| SonarQube | SAST          | Analyse statique du code                         |
| Trivy     | SCA/Container | Scan vulnérabilités dépendances et images Docker |

### SonarQube - Analyse Statique

```bash
# Lancer SonarQube (inclus dans docker-compose)
docker-compose up sonarqube -d

# Attendre le démarrage (~2 minutes)
# Accès: http://localhost:9000 (admin/admin)

# Analyse des services via Docker (pas besoin de Maven local)
docker run --rm \
  --network project_system_distribuer_ecommerce-network \
  -v "$(pwd)/product-service:/app" \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn sonar:sonar \
    -Dsonar.host.url=http://sonarqube:9000 \
    -Dsonar.token=<VOTRE_TOKEN> \
    -Dsonar.projectKey=product-service
```

**Configuration du token:**

1. Se connecter à http://localhost:9000
2. My Account → Security → Generate Token
3. Copier le token généré

**Résultats obtenus:**

- Product Service: **16 fichiers** analysés
- Order Service: **23 fichiers** analysés

### Trivy - Scan de Vulnérabilités

```bash
# Scan des dépendances d'un service
docker run --rm \
  -v "$(pwd)/product-service:/project" \
  aquasec/trivy:latest fs /project \
    --scanners vuln \
    --format table

# Scan avec rapport JSON
docker run --rm \
  -v "$(pwd)/product-service:/project" \
  -v "$(pwd)/docs/trivy-reports:/reports" \
  aquasec/trivy:latest fs /project \
    --scanners vuln \
    --format json \
    --output /reports/product-service-trivy.json

# Scan d'une image Docker
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy:latest image product-service:latest
```

**Résultats des scans de vulnérabilités:**

| Service         | Total | Critical | High | Medium | Low |
| --------------- | ----- | -------- | ---- | ------ | --- |
| product-service | 43    | 3        | 19   | 15     | 6   |
| order-service   | 54    | 3        | 23   | 21     | 7   |

**Vulnérabilités critiques détectées:**

- CVE-2025-24813: Apache Tomcat - Remote Code Execution
- CVE-2024-1597: PostgreSQL JDBC - SQL Injection
- CVE-2024-38821: Spring Security WebFlux - Authorization Bypass

### Rapports DevSecOps

Les rapports sont générés dans `docs/trivy-reports/`:

- `product-service-trivy.json` - Vulnérabilités dépendances
- `order-service-trivy.json` - Vulnérabilités dépendances
- `product-service-docker-trivy.json` - Vulnérabilités image Docker

### Diagramme d'Architecture

Voir [docs/architecture-mermaid.md](docs/architecture-mermaid.md) pour les diagrammes Mermaid complets incluant:

- Vue d'ensemble du système
- Flux d'authentification OAuth2/OIDC
- Architecture interne des microservices
- Diagramme de déploiement Docker
- Pipeline DevSecOps

## Diagramme de Séquence - Création de Commande

```
┌──────────┐     ┌───────────┐     ┌────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client  │     │ Keycloak  │     │ API Gateway│     │ Svc Commande│     │ Svc Produit │
└────┬─────┘     └─────┬─────┘     └──────┬─────┘     └──────┬──────┘     └──────┬──────┘
     │                 │                  │                  │                   │
     │  1. Login       │                  │                  │                   │
     │────────────────>│                  │                  │                   │
     │                 │                  │                  │                   │
     │  2. JWT Token   │                  │                  │                   │
     │<────────────────│                  │                  │                   │
     │                 │                  │                  │                   │
     │  3. POST /api/commandes (JWT)      │                  │                   │
     │───────────────────────────────────>│                  │                   │
     │                 │                  │                  │                   │
     │                 │    4. Valider JWT│                  │                   │
     │                 │<─────────────────│                  │                   │
     │                 │                  │                  │                   │
     │                 │    5. JWT Valid  │                  │                   │
     │                 │─────────────────>│                  │                   │
     │                 │                  │                  │                   │
     │                 │                  │  6. Forward (JWT)│                   │
     │                 │                  │─────────────────>│                   │
     │                 │                  │                  │                   │
     │                 │                  │                  │ 7. Vérifier stock │
     │                 │                  │                  │──────────────────>│
     │                 │                  │                  │                   │
     │                 │                  │                  │ 8. Stock OK + Prix│
     │                 │                  │                  │<──────────────────│
     │                 │                  │                  │                   │
     │                 │                  │                  │ 9. Calculer total │
     │                 │                  │                  │                   │
     │                 │                  │                  │ 10. Sauvegarder   │
     │                 │                  │                  │                   │
     │                 │                  │ 11. Commande créée                   │
     │                 │                  │<─────────────────│                   │
     │                 │                  │                  │                   │
     │  12. Response   │                  │                  │                   │
     │<───────────────────────────────────│                  │                   │
     │                 │                  │                  │                   │
```

## Structure du Projet

```
Project_System_Distribuer/
├── api-gateway/              # Spring Cloud Gateway
├── product-service/          # Micro-service Produit
├── order-service/            # Micro-service Commande
├── frontend/                 # Application React
├── keycloak/                 # Configuration Keycloak
├── docker-compose.yml        # Orchestration Docker
├── devsecops/               # Scripts DevSecOps
└── docs/                    # Documentation
```

## Licence

Projet académique - Développement d'applications distribuées sécurisées
