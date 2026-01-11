# 📸 Guide DevSecOps - Captures et Procédures

## Vue d'Ensemble

Ce document décrit les captures d'écran à réaliser pour documenter la mise en place DevSecOps du projet.

---

## 1. 📊 SonarQube - Analyse Statique

### Accès
- **URL:** http://localhost:9000
- **Identifiants:** admin / admin

### Captures à réaliser

#### 1.1 Dashboard Principal
![SonarQube Dashboard](screenshots/sonarqube-dashboard.png)

**Éléments à capturer:**
- Liste des projets analysés (product-service, order-service)
- Statut Quality Gate (Passed/Failed)
- Métriques globales

#### 1.2 Détail d'un Projet
![SonarQube Project Detail](screenshots/sonarqube-project.png)

**Métriques à capturer:**
- Bugs
- Vulnerabilities
- Code Smells
- Security Hotspots
- Coverage
- Duplications

#### 1.3 Génération du Token
![SonarQube Token](screenshots/sonarqube-token.png)

**Procédure:**
1. Cliquer sur l'icône profil en haut à droite
2. "My Account"
3. Onglet "Security"
4. "Generate Token"
5. Nommer le token et copier

### Commandes d'analyse

```bash
# Analyse Product Service
docker run --rm ^
  --network project_system_distribuer_ecommerce-network ^
  -v "%cd%\product-service:/app" ^
  -w /app ^
  maven:3.9-eclipse-temurin-17 ^
  mvn sonar:sonar ^
    -Dsonar.host.url=http://sonarqube:9000 ^
    -Dsonar.token=squ_43d75e12aa2d65bc70cdbb067c68fa6b6f7a23c8 ^
    -Dsonar.projectKey=product-service

# Analyse Order Service
docker run --rm ^
  --network project_system_distribuer_ecommerce-network ^
  -v "%cd%\order-service:/app" ^
  -w /app ^
  maven:3.9-eclipse-temurin-17 ^
  mvn sonar:sonar ^
    -Dsonar.host.url=http://sonarqube:9000 ^
    -Dsonar.token=squ_43d75e12aa2d65bc70cdbb067c68fa6b6f7a23c8 ^
    -Dsonar.projectKey=order-service
```

---

## 2. 🔍 Trivy - Scan de Vulnérabilités

### Captures à réaliser

#### 2.1 Scan des Dépendances
![Trivy Dependency Scan](screenshots/trivy-dependency-scan.png)

**Commande:**
```bash
docker run --rm ^
  -v "%cd%\product-service:/project" ^
  aquasec/trivy:latest fs /project ^
    --scanners vuln ^
    --format table
```

**Résultats attendus:**
| Sévérité | Count |
|----------|-------|
| CRITICAL | 3 |
| HIGH | 19 |
| MEDIUM | 15 |
| LOW | 6 |

#### 2.2 Scan d'Image Docker
![Trivy Image Scan](screenshots/trivy-image-scan.png)

**Commande:**
```bash
docker run --rm ^
  -v /var/run/docker.sock:/var/run/docker.sock ^
  aquasec/trivy:latest image product-service:latest
```

#### 2.3 Rapport HTML
![Trivy HTML Report](screenshots/trivy-html-report.png)

**Commande:**
```bash
docker run --rm ^
  -v "%cd%\product-service:/project" ^
  -v "%cd%\docs\trivy-reports:/reports" ^
  aquasec/trivy:latest fs /project ^
    --scanners vuln ^
    --format template ^
    --template "@contrib/html.tpl" ^
    --output /reports/product-service-trivy.html
```

---

## 3. 📁 Structure des Rapports

```
docs/
├── trivy-reports/
│   ├── product-service-trivy.json
│   ├── order-service-trivy.json
│   ├── product-service-docker-trivy.json
│   └── product-service-trivy.html
├── architecture-mermaid.md
├── architecture-diagram.md
└── devsecops-guide.md
```

---

## 4. 🛡️ Vulnérabilités Critiques Identifiées

### CVE-2025-24813 - Apache Tomcat RCE
- **Sévérité:** CRITICAL
- **Package:** org.apache.tomcat.embed:tomcat-embed-core
- **Impact:** Exécution de code à distance
- **Remédiation:** Mettre à jour vers Spring Boot 3.2.5+

### CVE-2024-1597 - PostgreSQL JDBC SQL Injection
- **Sévérité:** CRITICAL
- **Package:** org.postgresql:postgresql
- **Impact:** Injection SQL
- **Remédiation:** Mettre à jour vers postgresql 42.7.2+

### CVE-2024-38821 - Spring Security WebFlux
- **Sévérité:** CRITICAL
- **Package:** spring-security-webflux
- **Impact:** Bypass d'autorisation
- **Remédiation:** Mettre à jour vers Spring Security 6.3.3+

---

## 5. 📊 Métriques DevSecOps

### Tableau de Bord

| Métrique | Product Service | Order Service |
|----------|-----------------|---------------|
| Fichiers analysés (SonarQube) | 16 | 23 |
| Vulnérabilités totales (Trivy) | 43 | 54 |
| Vulnérabilités critiques | 3 | 3 |
| Vulnérabilités hautes | 19 | 23 |

### Quality Gate SonarQube

| Critère | Seuil | Statut |
|---------|-------|--------|
| Bugs | 0 | ✅ |
| Vulnerabilities | 0 | ⚠️ |
| Code Smells | <10 | ✅ |
| Coverage | >80% | ⚠️ |
| Duplications | <3% | ✅ |

---

## 6. 🔄 Pipeline CI/CD Recommandé

```yaml
# .github/workflows/devsecops.yml
name: DevSecOps Pipeline

on: [push, pull_request]

jobs:
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: SonarQube Scan
        uses: sonarsource/sonarcloud-github-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          
      - name: Trivy Dependency Scan
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          severity: 'CRITICAL,HIGH'
          
      - name: Trivy Image Scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'product-service:latest'
          severity: 'CRITICAL,HIGH'
```

---

## 7. 📝 Checklist DevSecOps

- [x] SonarQube container configuré dans docker-compose.yml
- [x] SonarQube accessible sur http://localhost:9000
- [x] Token d'analyse généré
- [x] Product Service analysé
- [x] Order Service analysé
- [x] Trivy scan dépendances product-service
- [x] Trivy scan dépendances order-service
- [x] Trivy scan image Docker
- [x] Rapports JSON générés
- [x] Documentation architecture Mermaid
- [x] Guide DevSecOps créé

---

## 8. 📸 Comment Prendre les Captures

### Windows (Snipping Tool)
```
Win + Shift + S
```

### Enregistrer dans
```
docs/screenshots/
```

### Naming Convention
- `sonarqube-dashboard.png`
- `sonarqube-project-product.png`
- `sonarqube-project-order.png`
- `sonarqube-token.png`
- `trivy-scan-product.png`
- `trivy-scan-order.png`
- `trivy-image-scan.png`
