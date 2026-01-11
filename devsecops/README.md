# DevSecOps - Configuration et Scripts

Ce dossier contient les outils et scripts pour l'analyse de sécurité du projet.

## Outils utilisés

### 1. SonarQube - Analyse statique du code

SonarQube analyse le code source pour détecter:
- Bugs potentiels
- Vulnérabilités de sécurité
- Code smells
- Duplication de code
- Couverture de tests

**Démarrage:**
```bash
# Démarrer SonarQube (décommenter dans docker-compose.yml)
docker-compose up -d sonarqube

# Attendre que SonarQube soit prêt (environ 2 minutes)
# Accéder à http://localhost:9000 (admin/admin)
```

**Lancer l'analyse:**
```bash
# Linux/macOS
./devsecops/sonar-analysis.sh

# Windows (dans chaque service)
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000
```

### 2. OWASP Dependency-Check - Analyse des dépendances

OWASP Dependency-Check identifie les vulnérabilités connues dans les dépendances.

**Lancer l'analyse:**
```bash
# Linux/macOS
./devsecops/owasp-check.sh

# Windows (dans chaque service)
mvn dependency-check:check
```

**Rapports générés:**
- `target/dependency-check-report.html` dans chaque service

### 3. Trivy - Scan des images Docker

Trivy scanne les images Docker pour détecter:
- Vulnérabilités OS
- Vulnérabilités des packages applicatifs
- Mauvaises configurations

**Installation:**
```bash
# Windows (Chocolatey)
choco install trivy

# macOS
brew install trivy

# Linux
sudo apt-get install trivy
```

**Lancer l'analyse:**
```bash
# Linux/macOS
./devsecops/trivy-scan.sh

# Windows
trivy image product-service:latest
trivy image order-service:latest
trivy image api-gateway:latest
trivy image frontend:latest
```

## Pipeline CI/CD recommandé

```yaml
# Exemple pour GitHub Actions
name: Security Analysis

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: OWASP Dependency Check
        run: |
          cd product-service && mvn dependency-check:check
          cd ../order-service && mvn dependency-check:check
          cd ../api-gateway && mvn dependency-check:check
      
      - name: Build Docker images
        run: docker-compose build
      
      - name: Trivy Scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'product-service:latest'
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: SonarQube Scan
        uses: sonarsource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

## Bonnes pratiques DevSecOps

1. **Shift Left**: Intégrer la sécurité dès le début du développement
2. **Automatisation**: Exécuter les scans à chaque commit
3. **Fail Fast**: Bloquer les builds avec des vulnérabilités critiques
4. **Traçabilité**: Conserver l'historique des scans
5. **Remediation**: Corriger rapidement les vulnérabilités identifiées

## Seuils recommandés

| Sévérité | Action |
|----------|--------|
| CRITICAL | Bloquer le déploiement |
| HIGH | Corriger sous 7 jours |
| MEDIUM | Corriger sous 30 jours |
| LOW | Planifier pour correction |

## Rapports

Les rapports sont générés dans:
- `devsecops/reports/owasp/` - Rapports OWASP
- `devsecops/reports/trivy/` - Rapports Trivy
- SonarQube Dashboard - http://localhost:9000
