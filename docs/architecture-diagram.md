# Diagramme d'Architecture - Application E-Commerce

## Architecture Globale

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    UTILISATEURS                                          │
│                            (Navigateurs Web / Mobile)                                    │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                          │
                                          │ HTTPS (Port 3000)
                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                               FRONTEND REACT                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌───────────────────┐   │
│  │   Dashboard     │  │   Produits      │  │   Commandes     │  │   Auth (Keycloak) │   │
│  │                 │  │   CRUD          │  │   Création      │  │   Login/Logout    │   │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └───────────────────┘   │
│                                                                                          │
│  Technologies: React 18, Material-UI, Keycloak-JS, Axios, React Router                   │
└─────────────────────────────────────────────────────────────────────────────────────────┘
           │                                                              │
           │ HTTP/REST (Port 8080)                                        │ OpenID Connect
           ▼                                                              ▼
┌──────────────────────────────────┐                    ┌─────────────────────────────────┐
│         API GATEWAY              │◄──────────────────►│          KEYCLOAK               │
│     (Spring Cloud Gateway)       │    JWT Validation   │   (Identity Provider)           │
│                                  │                    │                                 │
│  • Routage des requêtes          │                    │  • OAuth2 / OpenID Connect      │
│  • Validation JWT                │                    │  • Gestion des utilisateurs     │
│  • Rate Limiting (optionnel)     │                    │  • Rôles: ADMIN, CLIENT         │
│  • CORS Configuration            │                    │  • Session Management           │
│  • Logging / Traçabilité         │                    │                                 │
│                                  │                    │  Port: 8180                     │
│  Port: 8080                      │                    └─────────────────────────────────┘
└──────────────────────────────────┘
           │                    │
           │                    │
           ▼                    ▼
┌──────────────────────┐    ┌──────────────────────────────────────────────────────────────┐
│   PRODUCT SERVICE    │    │                    ORDER SERVICE                              │
│   (Spring Boot)      │◄───│                    (Spring Boot)                              │
│                      │    │                                                              │
│  Endpoints:          │ REST│  Endpoints:                                                  │
│  GET  /api/produits  │    │  POST /api/commandes           (CLIENT)                      │
│  GET  /api/produits/ │    │  GET  /api/commandes/mes-commandes (CLIENT)                  │
│       {id}           │    │  GET  /api/commandes           (ADMIN)                       │
│  POST /api/produits  │    │  GET  /api/commandes/{id}                                    │
│       (ADMIN)        │    │  POST /api/commandes/{id}/annuler                            │
│  PUT  /api/produits/ │    │                                                              │
│       {id} (ADMIN)   │    │  Fonctionnalités:                                            │
│  DELETE /api/produits│    │  • Vérification stock (via Product Service)                  │
│       /{id} (ADMIN)  │    │  • Calcul montant total                                      │
│                      │    │  • Gestion des états                                         │
│  Stock Management:   │    │  • Rollback stock si annulation                              │
│  POST /stock/check   │    │                                                              │
│  POST /stock/        │    │  Port: 8082                                                  │
│       decrement      │    └──────────────────────────────────────────────────────────────┘
│  POST /stock/        │                          │
│       increment      │                          │
│                      │                          │
│  Port: 8081          │                          │
└──────────────────────┘                          │
           │                                      │
           ▼                                      ▼
┌──────────────────────┐              ┌──────────────────────┐
│   PostgreSQL         │              │   PostgreSQL         │
│   (Product DB)       │              │   (Order DB)         │
│                      │              │                      │
│  Tables:             │              │  Tables:             │
│  • products          │              │  • orders            │
│                      │              │  • order_items       │
│  Port: 5432          │              │  Port: 5433          │
└──────────────────────┘              └──────────────────────┘
```

## Flux d'Authentification

```
┌──────────┐         ┌───────────┐         ┌──────────────┐
│  Client  │         │  Frontend │         │   Keycloak   │
└────┬─────┘         └─────┬─────┘         └──────┬───────┘
     │                     │                      │
     │  1. Accès app       │                      │
     │────────────────────>│                      │
     │                     │                      │
     │                     │  2. Redirect login   │
     │                     │─────────────────────>│
     │                     │                      │
     │  3. Page login      │                      │
     │<───────────────────────────────────────────│
     │                     │                      │
     │  4. Credentials     │                      │
     │───────────────────────────────────────────>│
     │                     │                      │
     │                     │  5. Auth Code        │
     │<───────────────────────────────────────────│
     │                     │                      │
     │                     │  6. Exchange Code    │
     │                     │─────────────────────>│
     │                     │                      │
     │                     │  7. JWT Tokens       │
     │                     │<─────────────────────│
     │                     │                      │
     │  8. App + Tokens    │                      │
     │<────────────────────│                      │
     │                     │                      │
```

## Flux de Communication Sécurisé

```
┌──────────┐     ┌───────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ Frontend │     │ Keycloak  │     │ API Gateway │     │Product Svc  │     │ Order Svc   │
└────┬─────┘     └─────┬─────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
     │                 │                  │                   │                   │
     │  JWT Token      │                  │                   │                   │
     ├────────────────────────────────────>                   │                   │
     │                 │                  │                   │                   │
     │                 │   Validate JWT   │                   │                   │
     │                 │<─────────────────│                   │                   │
     │                 │                  │                   │                   │
     │                 │   JWT Valid ✓    │                   │                   │
     │                 │─────────────────>│                   │                   │
     │                 │                  │                   │                   │
     │                 │                  │  Forward + JWT    │                   │
     │                 │                  │──────────────────>│                   │
     │                 │                  │                   │                   │
     │                 │                  │     Response      │                   │
     │                 │                  │<──────────────────│                   │
     │                 │                  │                   │                   │
     │     Response    │                  │                   │                   │
     │<───────────────────────────────────│                   │                   │
     │                 │                  │                   │                   │
```

## Architecture DevSecOps

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                               PIPELINE CI/CD                                             │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                          │
        ┌─────────────────────────────────┼─────────────────────────────────┐
        │                                 │                                 │
        ▼                                 ▼                                 ▼
┌───────────────────┐           ┌───────────────────┐           ┌───────────────────┐
│    SONARQUBE      │           │  OWASP DEP-CHECK  │           │      TRIVY        │
│                   │           │                   │           │                   │
│  Analyse statique │           │  Vulnérabilités   │           │  Scan images      │
│  • Bugs           │           │  dépendances      │           │  Docker           │
│  • Vulnérabilités │           │  • CVE database   │           │  • OS vuln        │
│  • Code smells    │           │  • NVD            │           │  • App vuln       │
│  • Duplication    │           │                   │           │                   │
│                   │           │  Maven plugin     │           │  CLI tool         │
│  Port: 9000       │           │                   │           │                   │
└───────────────────┘           └───────────────────┘           └───────────────────┘
```

## Ports et Services

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 3000 | Application React |
| API Gateway | 8080 | Point d'entrée API |
| Product Service | 8081 | Gestion des produits |
| Order Service | 8082 | Gestion des commandes |
| Keycloak | 8180 | Serveur d'authentification |
| PostgreSQL (Products) | 5432 | Base de données produits |
| PostgreSQL (Orders) | 5433 | Base de données commandes |
| SonarQube | 9000 | Analyse de code (DevSecOps) |
