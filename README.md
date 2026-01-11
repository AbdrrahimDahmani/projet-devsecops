# Plateforme E-Commerce Microservices

> Une application e-commerce sécurisée et distribuée, construite avec une architecture microservices et intégrant un pipeline DevSecOps.

[![Pipeline DevSecOps](https://github.com/AbdrrahimDahmani/projet-devsecops/actions/workflows/devsecops.yml/badge.svg)](https://github.com/AbdrrahimDahmani/projet-devsecops/actions/workflows/devsecops.yml)

## Auteurs

- **Abdrrahim Dahmani** - [@AbdrrahimDahmani](https://github.com/AbdrrahimDahmani)
- **Driss Ait Maali** - [@drissrad](https://github.com/drissrad)

---

## Architecture

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
│     SERVICE PRODUITS            │  │     SERVICE COMMANDES               │
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
│                  (Gestion des Identités et Accès)                        │
│                         Port: 8180                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

## Stack Technique

| Composant                 | Technologie                   | Port |
| ------------------------- | ----------------------------- | ---- |
| Frontend                  | React 18 + Vite + Keycloak JS | 3000 |
| API Gateway               | Spring Cloud Gateway 3.2      | 8080 |
| Service Produits          | Spring Boot 3.2               | 8081 |
| Service Commandes         | Spring Boot 3.2               | 8082 |
| Fournisseur d'Identité    | Keycloak 23                   | 8180 |
| Base de Données Produits  | PostgreSQL 15                 | 5432 |
| Base de Données Commandes | PostgreSQL 15                 | 5433 |

## Sécurité

### Authentification & Autorisation

- **OAuth2 / OpenID Connect** via Keycloak
- **Propagation du Token JWT** entre les services
- **Contrôle d'accès basé sur les rôles** (ADMIN, CLIENT)

### Permissions API

| Endpoint                           | ADMIN | CLIENT |
| ---------------------------------- | ----- | ------ |
| `GET /api/produits`                | ✅    | ✅     |
| `GET /api/produits/{id}`           | ✅    | ✅     |
| `POST /api/produits`               | ✅    | ❌     |
| `PUT /api/produits/{id}`           | ✅    | ❌     |
| `DELETE /api/produits/{id}`        | ✅    | ❌     |
| `POST /api/commandes`              | ❌    | ✅     |
| `GET /api/commandes/mes-commandes` | ❌    | ✅     |
| `GET /api/commandes`               | ✅    | ❌     |

## Démarrage Rapide

### Prérequis

- Docker & Docker Compose
- Java 17+
- Node.js 18+
- Maven 3.8+

### Lancement avec Docker Compose

```bash
# Construire et démarrer tous les services
docker-compose up --build

# Lancer en arrière-plan
docker-compose up -d --build

# Arrêter tous les services
docker-compose down
```

### URLs d'Accès

| Service        | URL                         |
| -------------- | --------------------------- |
| Frontend       | http://localhost:3000       |
| API Gateway    | http://localhost:8080       |
| Admin Keycloak | http://localhost:8180/admin |

### Utilisateurs de Test

| Utilisateur | Mot de passe | Rôle   |
| ----------- | ------------ | ------ |
| admin       | admin123     | ADMIN  |
| client      | client123    | CLIENT |

Admin Keycloak : `admin` / `admin`

---

## Pipeline DevSecOps

Notre pipeline CI/CD s'exécute automatiquement à chaque push et pull request sur la branche `main`.

### Étapes du Pipeline

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Build &    │───▶│   OWASP     │───▶│  SonarCloud │───▶│   Docker    │───▶│   Trivy     │
│    Test     │    │ Dep Check   │    │   Analyse   │    │   Build     │    │   Scan      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

| Étape                      | Outil      | Objectif                                      |
| -------------------------- | ---------- | --------------------------------------------- |
| **Build & Test**           | Maven      | Compilation et exécution des tests unitaires  |
| **OWASP Dependency Check** | OWASP DC   | Scan des dépendances pour les CVE connues     |
| **Analyse SonarCloud**     | SonarCloud | Analyse statique du code (SAST)               |
| **Build Docker**           | Docker     | Construction des images conteneurs            |
| **Scan de Sécurité Trivy** | Trivy      | Scan des vulnérabilités conteneurs & fichiers |

### Outils de Sécurité

| Outil                      | Type          | Description                                                        |
| -------------------------- | ------------- | ------------------------------------------------------------------ |
| **SonarCloud**             | SAST          | Test de Sécurité Applicative Statique - qualité & sécurité du code |
| **OWASP Dependency-Check** | SCA           | Analyse de Composition Logicielle - vulnérabilités des dépendances |
| **Trivy**                  | Conteneur/SCA | Scanner de vulnérabilités complet pour conteneurs & code           |

### Configuration du Pipeline

Le pipeline est défini dans [`.github/workflows/devsecops.yml`](.github/workflows/devsecops.yml).

Les résultats de sécurité sont automatiquement téléversés dans l'onglet **GitHub Security** (format SARIF).

---

## Structure du Projet

```
├── .github/
│   └── workflows/
│       └── devsecops.yml       # Pipeline CI/CD
├── api-gateway/                # Spring Cloud Gateway
├── product-service/            # Microservice Produits
├── order-service/              # Microservice Commandes
├── frontend/                   # Application React
├── keycloak/                   # Configuration realm Keycloak
├── docs/                       # Documentation
│   ├── architecture-mermaid.md
│   ├── api-documentation.md
│   ├── sequence-diagram.md
│   └── trivy-reports/
├── docker-compose.yml          # Orchestration Docker
└── sonar-project.properties    # Configuration SonarCloud
```

## Développement Local

### Lancer les Services Individuellement

```bash
# Service Produits
cd product-service
mvn spring-boot:run

# Service Commandes
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

## Diagramme de Séquence - Création de Commande

```
┌──────────┐     ┌───────────┐     ┌────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client  │     │ Keycloak  │     │ API Gateway│     │Svc Commandes│     │Svc Produits │
└────┬─────┘     └─────┬─────┘     └──────┬─────┘     └──────┬──────┘     └──────┬──────┘
     │                 │                  │                  │                   │
     │  1. Connexion   │                  │                  │                   │
     │────────────────>│                  │                  │                   │
     │                 │                  │                  │                   │
     │  2. Token JWT   │                  │                  │                   │
     │<────────────────│                  │                  │                   │
     │                 │                  │                  │                   │
     │  3. POST /api/commandes + JWT      │                  │                   │
     │───────────────────────────────────>│                  │                   │
     │                 │                  │                  │                   │
     │                 │    4. Valider JWT│                  │                   │
     │                 │<─────────────────│                  │                   │
     │                 │                  │                  │                   │
     │                 │    5. JWT Valide │                  │                   │
     │                 │─────────────────>│                  │                   │
     │                 │                  │                  │                   │
     │                 │                  │  6. Transférer+JWT                   │
     │                 │                  │─────────────────>│                   │
     │                 │                  │                  │                   │
     │                 │                  │                  │ 7. Vérifier stock │
     │                 │                  │                  │──────────────────>│
     │                 │                  │                  │                   │
     │                 │                  │                  │ 8. Stock OK       │
     │                 │                  │                  │<──────────────────│
     │                 │                  │                  │                   │
     │                 │                  │ 9. Commande créée│                   │
     │                 │                  │<─────────────────│                   │
     │                 │                  │                  │                   │
     │  10. Réponse    │                  │                  │                   │
     │<───────────────────────────────────│                  │                   │
```

---

## Documentation

- [Diagrammes d'Architecture](docs/architecture-mermaid.md)
- [Documentation API](docs/api-documentation.md)
- [Diagrammes de Séquence](docs/sequence-diagram.md)
- [Guide DevSecOps](docs/devsecops-guide.md)

---

## Licence

Projet Académique - Développement d'Applications Distribuées Sécurisées

**ENSET Mohammedia** - 2025/2026
